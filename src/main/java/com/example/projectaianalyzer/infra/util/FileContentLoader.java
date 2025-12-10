package com.example.projectaianalyzer.infra.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileContentLoader {
    public static String loadFileContent(Path filePath) {
//        log.debug("::::load file content: 파일의 코드 전체를 가져옵니다.:::");
        String content;
        try {
            content = Files.readString(filePath);
        } catch (IOException e) {
            log.warn("파일을 읽을 수 없음 : {}", e.getMessage());
            return "";
        }
        if (content.isBlank() || content.isEmpty() || content.length() == 1) {
            return "";
        }
        return content;
    }
}
