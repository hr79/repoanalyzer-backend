package com.example.projectaianalyzer.domain.ai.service;

import com.example.projectaianalyzer.domain.project.model.FileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class OpenAiClient {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public String sendToOpenAi(List<FileInfo> fileInfos) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> messageUser = new HashMap<>();
        messageUser.put("role", "user");
        messageUser.put("content", "다음 JSON파일의 속성 값들을 기반으로 각 파일들의 코드가 어떤 로직인지 분석해:\n" + fileInfos.toString());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o"); // 모델명은 사용가능한 것으로 바꿔야 함
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "당신은 소프트웨어 엔지니어, 백엔드 개발자입니다."),
                messageUser
        ));
        requestBody.put("temperature", 0.3);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<?> choices = (List<?>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                if (message != null) {
                    return (String) message.get("content");
                }
            }
        }

        throw new RuntimeException("OpenAI API 응답에 실패했습니다: " + response);
    }
}
