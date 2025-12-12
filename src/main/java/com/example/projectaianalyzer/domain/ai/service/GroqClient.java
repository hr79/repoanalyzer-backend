package com.example.projectaianalyzer.domain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class GroqClient {
    private final RestTemplate restTemplate;

    @Value("${groq.api-key}")
    private String groqApiKey;

    // 재시도 관련 설정 (application.properties 또는 application.yml로 오버라이드 가능)
    @Value("${groq.retry.max:5}")
    private int maxRetries;

    @Value("${groq.retry.base-delay-ms:800}")
    private long baseDelayMs;

    @Value("${groq.retry.max-jitter-ms:400}")
    private long maxJitterMs;

    @Value("${groq.retry.fallback-threshold-ms:12000}")
    private long fallbackThresholdMs;

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
//    private final Random random = new Random();

    public GroqClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzeProject(Object jsonObject, Map<String, Object> systemMessage, Map<String, Object> userMessage, String model, @Nullable String fallbackModel) {
        log.info("::::GroqClient.analyzeProject 호출됨");

        HttpEntity<Map<String, Object>> entity = buildHttpRequestBody(model, systemMessage, userMessage);

        // 재시도 루프: 429 또는 서버 오류(5xx)에 대해 지수 백오프 + 지터로 재시도
        return callWithFallback(fallbackModel, entity, model);
    }

    private String callWithFallback(String fallbackModel, HttpEntity<Map<String, Object>> entity, String model) {
        try {
            return sendRequest(entity, model, fallbackModel);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private HttpEntity<Map<String, Object>> buildHttpRequestBody(String currentModel, Map<String, Object> systemMessage, Map<String, Object> userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", currentModel);
        body.put("messages", List.of(systemMessage, userMessage));
        body.put("temperature", 0.3);

        return new HttpEntity<>(body, headers);
    }

    private String sendRequest(HttpEntity<Map<String, Object>> entity, String model, String fallbackModel) {
        // 요청 헤더 설정
        int attempt = 0;
        long waitMs = 0L;
        boolean enableFallback = true;
        String currentModel = model;

        while (attempt <= maxRetries) {
            if (attempt > 0) {
                log.info("재시도 {}회째", attempt);
            }
            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        GROQ_API_URL,
                        HttpMethod.POST,
                        entity,
                        Map.class
                );

                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

                        return (String) message.get("content");
                    }
                }
            } catch (HttpClientErrorException e) {
                // 4xx 에러 처리
                if (e.getStatusCode().value() == 429) {
                    attempt++;
                    if (attempt > maxRetries) {
                        log.error("error: {}, code: {}, message: {}", e.getStatusCode(), e.getRawStatusCode(), e.getMessage());
                        throw new RuntimeException("429 Too Many Requests - 최대 재시도 횟수 초과", e);
                    }

                    // Retry-After 헤더 우선 처리
                    String retryAfter = e.getResponseHeaders() != null ?
                            e.getResponseHeaders().getFirst("Retry-After") : null;
                    waitMs = computeBackoffWithJitter(attempt);
                    long maxWaitMs = Duration.ofSeconds(20).toMillis(); // 최대 대기 시간: 20초(=20,000ms)
                    if (retryAfter != null) {
                        try {
                            // Retry-After가 초 단위 숫자일 경우
                            long seconds = Long.parseLong(retryAfter);
                            waitMs = Math.max(waitMs, Duration.ofSeconds(seconds).toMillis()); // retry-after가 더 길면 우선 적용
                        } catch (NumberFormatException ex) {
                            // 날짜 형식의 Retry-After는 파싱을 건너뜀(기본 backoff 사용)
                        }
                    }

                    if (waitMs >= fallbackThresholdMs && enableFallback) {
                        log.info("대기 시간이 {}ms로 임계값 {}ms를 초과하여 모델을 {} -> {}로 전환합니다.", waitMs, fallbackThresholdMs, currentModel, fallbackModel);
                        currentModel = fallbackModel;
                        attempt = 0;
                        waitMs = 0L;
                        enableFallback = false;
                        entity.getBody().put("model", currentModel);
                    } else if (waitMs >= fallbackThresholdMs && !enableFallback) {
                        log.warn("enableFallback == false: fallback 모델로 전환할 수 없습니다");
                    }

                    log.info("429 응답 - 재시도 대기(ms): {} (시도: {}), 현재 모델: {}", waitMs, attempt, currentModel);
                    sleepMillis(waitMs);
                }
            } catch (HttpServerErrorException e) {
                attempt++;
                if (attempt > maxRetries) {
                    throw new RuntimeException("5xx 서버 오류 - 최대 재시도 횟수 초과", e);
                }
                waitMs = computeBackoffWithJitter(attempt);
                log.warn("서버 오류({}) - 재시도 대기(ms): {} (시도: {}), 현재 모델: {}", e.getRawStatusCode(), waitMs, attempt, currentModel);
                log.warn("error: {}, message: {}", e.getStatusCode(), e.getMessage());
                sleepMillis(waitMs);
            } catch (Exception e) {
                log.error("현재 모델: {}, Groq API 호출 중 오류 발생: {}", currentModel, e.getMessage());
                throw new RuntimeException("Groq API 호출 중 오류 발생", e);
            }
        }

        return "분석 결과를 가져오지 못했습니다.";
    }

    private long computeBackoffWithJitter(int attempt) {
        // 지수 백오프: baseDelayMs * 2^(attempt-1)
        long expBackoff = baseDelayMs * (1L << Math.max(0, attempt - 1));
        // 랜덤 지터 추가
        long jitter = (maxJitterMs > 0) ? (long) (ThreadLocalRandom.current().nextDouble() * maxJitterMs) : 0L;

        // 최대 대기 시간 보호(예: 20초)
        long maxWait = Duration.ofSeconds(20).toMillis();
        return Math.min(expBackoff + jitter, maxWait);
    }

    private void sleepMillis(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
        }
    }
}

