package com.example.projectaianalyzer.infra.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonParser {
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public <T> T parseJson(String content, TypeReference<T> typeReference) {
        log.debug("::::parseJson::: content length={}", content == null ? 0 : content.length());
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is empty");
        }
        String jsonData = JsonStringExtractor.extractJsonFromString(content);
        try {
            return objectMapper.readValue(jsonData, typeReference);
        } catch (JsonProcessingException e) {
            log.error(":::: JSON 파싱 실패 :::: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
