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
    public <T> T parseJson(String content, TypeReference<T> typeReference) {
        log.info("::::parseJson:::");
        if (content == null || content.isEmpty()) {
            return null;
        }
        String jsonData = JsonStringExtractor.extractJsonFromString(content);
        log.info("추출된 jsonData: {}", jsonData);
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return objectMapper.readValue(jsonData, typeReference);
        } catch (JsonProcessingException e) {
            log.error(":::: JSON 파싱 실패 ::::");
            throw new RuntimeException(e);
        }
    }
}
