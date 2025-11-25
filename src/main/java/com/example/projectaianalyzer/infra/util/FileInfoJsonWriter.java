package com.example.projectaianalyzer.infra.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileInfoJsonWriter {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT); // 보기 좋게 정렬된 JSON 출력

    public static void writeToJsonFile(Object fileInfos, String outputFilePath) {
        System.out.println("::::json파일 생성 시작");
        try {
            File outputFile = new File(outputFilePath);

            // 존재하지 않는 디렉토리라면 자동 생성
            Path parentPath = Paths.get(outputFilePath).getParent();
            if (parentPath != null){
                Files.createDirectories(parentPath);
            }
            objectMapper.writeValue(outputFile, fileInfos);
            System.out.println("✅ JSON 파일 생성 완료: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException("JSON 파일 생성 중 오류 발생", e);
        }
    }
}
