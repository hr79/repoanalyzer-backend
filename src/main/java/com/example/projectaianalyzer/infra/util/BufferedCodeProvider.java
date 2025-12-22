package com.example.projectaianalyzer.infra.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Slf4j
public class BufferedCodeProvider implements CodeProvider {
    @Override
    public String loadContent(String filePath) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {
            log.info("파일 내용을 읽어오는 중: {}", filePath);
            reader.lines()
                    .filter(line -> !line.startsWith("package "))
                    .filter(line -> !line.startsWith("import "))
                    .limit(400)
                    .forEach(line -> {
                        if (sb.length() < 10_000) {
                            sb.append(line).append("\n");
                        }
                    });
        } catch (IOException e) {
            log.warn("파일 내용을 불러오는 중 오류 발생: {}", e.getMessage(), e);
        }
        return sb.toString();
    }
}
