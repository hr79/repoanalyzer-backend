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
import org.springframework.util.FileSystemUtils;

@Component
@Slf4j
public class FileStorageImpl implements FileStorage {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT); // 보기 좋게 정렬된 JSON 출력

    @Override
    public void writeJson(Object data, String outputFilePath) {
        log.info(":::: json 파일 생성 시작: {}", outputFilePath);
        try {
            Path parentPath = Paths.get(outputFilePath).getParent();
            if (parentPath != null) {
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
        log.info("::: delete file or directory : {}", stringPath);

        Path path = Paths.get(stringPath).toAbsolutePath();
        // 권한체크
        checkPermissions(stringPath);

        if (Files.notExists(path)) {
            log.error("파일이나 폴더 경로가 존재하지 않습니다.: {}", path);
            return;
        }
        try {
            log.info("{} 파일 및 디렉토리 삭제를 시작합니다.", path);
            boolean deleted = FileSystemUtils.deleteRecursively(path);
            if (!deleted) { // deleted == false 삭제 실패함
                log.error("Failed to delete: {}", path);
            }
        } catch (Exception e) {
            log.error("Failed to delete: {}", path, e);
        }
    }

    private static void checkPermissions(String stringPath) {
        Path path = Paths.get(stringPath).toAbsolutePath();

        if (!Files.exists(path)) {
            System.out.println("경로가 존재하지 않습니다: " + path);
            return;
        }

        System.out.println("경로: " + path);
        System.out.println("읽기 가능: " + Files.isReadable(path));
        System.out.println("쓰기 가능: " + Files.isWritable(path));
        System.out.println("실행 가능: " + Files.isExecutable(path));
    }
}
