package com.example.projectaianalyzer.domain.project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
@Slf4j
public class ProjectWalker {
    public Stream<Path> walkFiles(Path rootPath, int maxDepth){
        try {
            return Files.walk(rootPath, maxDepth).filter(Files::isRegularFile);
        } catch (IOException e) {
            log.error("프로젝트 폴더를 스캔할 수 없습니다: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Stream<Path> walkFiles(Path rootPath){
        try {
            return Files.walk(rootPath).filter(Files::isRegularFile);
        } catch (IOException e) {
            log.error("프로젝트 폴더를 스캔할 수 없습니다: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
