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

    public GroqClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzeProject(Object jsonObject, Map<String, Object> systemMessage, Map<String, Object> userMessage, String model, @Nullable String fallbackModel) {
        log.info("::::GroqClient.analyzeProject 호출됨, 모델: {}", model);

        HttpEntity<Map<String, Object>> entity = buildHttpRequestBody(model, systemMessage, userMessage);

        // 재시도 루프: 429 또는 서버 오류(5xx)에 대해 지수 백오프 + 지터로 재시도
        String result = callWithFallback(fallbackModel, entity, model);
        log.info("::::GroqClient.analyzeProject 완료");
        return result;
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

        log.info(":::: sendRequest 시작, 초기 모델: {}", currentModel);

        while (attempt <= maxRetries) {
            if (attempt > 0) {
                log.info("재시도 {}회째", attempt);
            }
            try {
                log.info(":::: Groq API 호출 시작 (시도: {}), 모델: {}, 스레드: {}", attempt, currentModel, Thread.currentThread().getName());

                ResponseEntity<Map> response = restTemplate.exchange(
                        GROQ_API_URL,
                        HttpMethod.POST,
                        entity,
                        Map.class
                );

                log.info(":::: Groq API 응답 수신 (시도: {}), 상태: {}", attempt, response.getStatusCode());

                // ✅ 응답 파싱을 별도 메서드로 분리
                // 파싱 실패 시 예외 발생 → 무한 루프 방지
                String content = parseResponse(response);

                log.info(":::: Groq API 응답 파싱 완료, 모델: {}", currentModel);
                return content;

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
                    waitWithThreadSleep(waitMs);
                    continue;  // ✅ 명시적 continue
                } else {
                    // ✅ 다른 4xx 에러는 재시도하지 않음
                    log.error("4xx 에러 ({}): {}", e.getStatusCode(), e.getMessage());
                    throw new RuntimeException("API 4xx 에러: " + e.getStatusCode(), e);
                }
            } catch (HttpServerErrorException e) {
                attempt++;
                if (attempt > maxRetries) {
                    throw new RuntimeException("5xx 서버 오류 - 최대 재시도 횟수 초과", e);
                }
                waitMs = computeBackoffWithJitter(attempt);
                log.warn("서버 오류({}) - 재시도 대기(ms): {} (시도: {}), 현재 모델: {}", e.getRawStatusCode(), waitMs, attempt, currentModel);
                log.warn("error: {}, message: {}", e.getStatusCode(), e.getMessage());
                waitWithThreadSleep(waitMs);
                continue;  // ✅ 명시적 continue
            } catch (RuntimeException e) {
                // ✅ 응답 파싱 실패 등 런타임 에러
                log.error("런타임 오류: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("현재 모델: {}, Groq API 호출 중 예기치 않은 오류 발생: {}", currentModel, e.getMessage(), e);
                throw new RuntimeException("Groq API 호출 중 오류 발생", e);
            }
        }

        log.warn(":::: sendRequest 완료 - 최대 재시도 초과");
        return "분석 결과를 가져오지 못했습니다.";
    }

    // ✅ 추가: 응답 파싱 메서드 (모든 실패 케이스 처리)
    private String parseResponse(ResponseEntity<Map> response) {
        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null) {
            log.warn("응답 본문이 null입니다");
            throw new RuntimeException("응답 본문이 null입니다");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

        if (choices == null || choices.isEmpty()) {
            log.warn("응답에 choices가 없거나 비어있습니다. 응답 키: {}", responseBody.keySet());
            throw new RuntimeException("응답에 choices가 없습니다");
        }

        Map<String, Object> choice = choices.get(0);

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choice.get("message");

        if (message == null) {
            log.warn("message가 null입니다. choice 키: {}", choice.keySet());
            throw new RuntimeException("message가 null입니다");
        }

        String content = (String) message.get("content");

        if (content == null || content.isBlank()) {
            log.warn("content가 null이거나 비어있습니다. message 키: {}", message.keySet());
            throw new RuntimeException("content가 없습니다");
        }

        return content;
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

    private void waitWithThreadSleep(long ms) {
        // ✅ 수정: Thread.sleep() 사용 - groqExecutor 스레드를 블로킹해도 괜찮음
        // (retryScheduler의 싱글 스레드에 의존하지 않음)
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("재시도 대기 중 인터럽트 발생: {}", e.getMessage());
        }
    }
}
