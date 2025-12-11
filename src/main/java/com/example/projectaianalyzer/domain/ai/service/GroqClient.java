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

@Slf4j
@Component
public class GroqClient {
    private final RestTemplate restTemplate;

    @Value("${groq.api-key}")
    private String groqApiKey;

    // 재시도 관련 설정 (application.properties 또는 application.yml로 오버라이드 가능)
    @Value("${groq.retry.max:3}")
    private int maxRetries;

    @Value("${groq.retry.base-delay-ms:1000}")
    private long baseDelayMs;

    @Value("${groq.retry.max-jitter-ms:500}")
    private long maxJitterMs;

    @Value("${groq.retry.fallback-threshold-ms:15000}")
    private long fallbackThresholdMs;

    @Value("${groq.retry.enable-fallback:true}")
    private boolean enableFallback;

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final Random random = new Random();

    public GroqClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzeProject(Object jsonObject, Map<String, Object> systemMessage, Map<String, Object> userMessage, String model, @Nullable String fallbackModel) {
        log.info("::::GroqClient.analyzeProject 호출됨");

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        String currentModel = model;

        Map<String, Object> body = new HashMap<>();
        body.put("model", currentModel);
        body.put("messages", List.of(systemMessage, userMessage));
        body.put("temperature", 0.3);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 재시도 루프: 429 또는 서버 오류(5xx)에 대해 지수 백오프 + 지터로 재시도
        int attempt = 0;
        while (true) {
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

                return "⚠️ 분석 결과를 가져오지 못했습니다.";

            } catch (HttpClientErrorException e) {
                // 4xx 에러 처리
                if (e.getStatusCode().value() == 429) {
                    attempt++;
                    if (attempt > maxRetries) {
                        throw new RuntimeException("429 Too Many Requests - 최대 재시도 횟수 초과", e);
                    }

                    // Retry-After 헤더 우선 처리
                    String retryAfter = e.getResponseHeaders() != null ? e.getResponseHeaders().getFirst("Retry-After") : null;
                    long waitMs = computeBackoffWithJitter(attempt);
                    long maxWaitMs = Duration.ofSeconds(30).toMillis(); // 최대 대기 시간: 1분(=60,000ms)
                    if (retryAfter != null) {
                        try {
                            // Retry-After가 초 단위 숫자일 경우
                            long seconds = Long.parseLong(retryAfter);
                            waitMs = Math.max(waitMs, Duration.ofSeconds(seconds).toMillis());
                        } catch (NumberFormatException ex) {
                            // 날짜 형식의 Retry-After는 파싱을 건너뜀(기본 backoff 사용)
                        }
                    }

                    // 대기 시간 상한 적용 (최대 1분)
                    if (waitMs > maxWaitMs) {
                        waitMs = maxWaitMs;
                    }

                    // 대기가 너무 길면 fallbackModel로 전환
                    if (enableFallback && currentModel.equals(model) && waitMs >= fallbackThresholdMs) {
                        System.out.println("429 응답 - 대기가 너무 길어 FALLBACK 모델로 전환: " + fallbackModel + " (기존: " + currentModel + ")");
                        currentModel = fallbackModel;
                        body.put("model", currentModel); // 같은 body 맵을 업데이트하면 entity에도 반영됨
                        attempt = 0; // 모델 변경 후 재시도 횟수 초기화
                        continue; // 즉시 재시도 (대기 없음)
                    }

                    System.out.println("429 응답 - 재시도 대기(ms): " + waitMs + " (시도: " + attempt + ")");
                    sleepMillis(waitMs);
                    continue; // 재시도
                }

                // 기타 4xx는 재시도하지 않음
                throw e;

            } catch (HttpServerErrorException e) {
                // 5xx 서버 오류는 재시도 가능
                attempt++;
                if (attempt > maxRetries) {
                    throw new RuntimeException("5xx 서버 오류 - 최대 재시도 횟수 초과", e);
                }
                long waitMs = computeBackoffWithJitter(attempt);
                System.out.println("서버 오류(" + e.getStatusCode() + ") - 재시도 대기(ms): " + waitMs + " (시도: " + attempt + ")");
                sleepMillis(waitMs);
                continue;

            } catch (Exception e) {
                throw new RuntimeException("Groq API 호출 중 오류 발생", e);
            }
        }

    }

    private long computeBackoffWithJitter(int attempt) {
        // 지수 백오프: baseDelayMs * 2^(attempt-1)
        long expBackoff = baseDelayMs * (1L << Math.max(0, attempt - 1));
        // 랜덤 지터 추가
        long jitter = (maxJitterMs > 0) ? (long) (random.nextDouble() * maxJitterMs) : 0L;
        // 최대 대기 시간 보호(예: 60초)
        long maxWait = Duration.ofSeconds(60).toMillis();
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
