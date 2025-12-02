package com.example.projectaianalyzer.infra.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class FileStorageImpl implements FileStorage{
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT); // 보기 좋게 정렬된 JSON 출력


    @Override
    public void writeJson(Object data,String outputFilePath) {
        log.info(":::: json 파일 생성 시작: {}", outputFilePath);
        try {
            Path parentPath = Paths.get(outputFilePath).getParent();
            if (parentPath != null){
                Files.createDirectories(parentPath);
            }
            File outputFile = new File(outputFilePath);
            objectMapper.writeValue(outputFile, data);
            log.info("✅ JSON 파일 생성 완료: {}", outputFile.getAbsolutePath());

        } catch (IOException e) {
            log.error("JSON 파일 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("JSON 파일 생성 중 오류 발생", e);
        }
    }

    @Override
    public void delete(String stringPath) {
        Path path = Paths.get(stringPath);
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                log.error("Failed to delete: {}", p, e);
                            }
                        });
            }
        } catch (IOException e) {
            log.error("Failed to delete: {}", path, e);
            throw new RuntimeException("Failed to delete: " + path, e);
        }
    }
}
