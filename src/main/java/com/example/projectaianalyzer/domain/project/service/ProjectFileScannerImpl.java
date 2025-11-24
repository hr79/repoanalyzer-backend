package com.example.projectaianalyzer.domain.project.service;

import com.example.projectaianalyzer.domain.analysis.service.AnalyzeServiceInterface.ProjectFileScanner;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import com.example.projectaianalyzer.domain.project.model.ProjectInfo;
import com.example.projectaianalyzer.infra.util.FileContentLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectFileScannerImpl implements ProjectFileScanner {
    private final ProjectFileClassifier projectFileClassifier;
    private final ProjectTypeDetector projectTypeDetector;

    @Override
    public List<FileInfo> scanProjectDirectory(String filePath, List<FileInfo> businessLogicFiles, List<String> fileStructureSummaries) {
        log.info("::::1. 프로젝트 폴더 스캔 시작::::");
        Path rootPath = Paths.get(filePath);
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("경로 정보가 없습니다.");
        }
        if (!Files.exists(rootPath)) {
            throw new IllegalArgumentException("스캔할 프로젝트 경로가 유효하지 않습니다.");
        }
        // 하위 디렉토리 탐색하며 backend, frontend, language, framework 감지
        List<ProjectInfo> projectInfoList = detectProjectTypeBySubDir(filePath);
        List<FileInfo> fileInfoList = new ArrayList<>();
        try {
            Files.walk(rootPath)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        log.info("::::path = {}", path);
                        ProjectInfo projectInfo = findMatchingProjectType(path, projectInfoList);
                        String extension = projectFileClassifier.classifyExtension(path);
                        String role = projectFileClassifier.classifyRole(path);

                        String rawCode = FileContentLoader.loadFileContent(path);
                        String content = getContent(rawCode);

                        FileInfo fileInfo = FileInfo.builder()
                                .fileName(path.getFileName().toString())
                                .relativePath(rootPath.relativize(path).toString())
                                .absolutePath(path.toAbsolutePath().toString())
                                .extension(extension)
                                .role(role)
                                .content(content)
                                .projectInfo(projectInfo)
                                .build();

                        // 파일구조 요약에 필요한 리스트에 상대경로 정보 추가
                        fileStructureSummaries.add(fileInfo.getRelativePath());
                        fileInfoList.add(fileInfo);
                    });
            return fileInfoList;
        } catch (IOException e) {
            throw new RuntimeException("프로젝트 폴더를 스캔할 수 없습니다.", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getContent(String rawCode) {
        return rawCode
                .replaceAll("package .*?;", "")
                .replaceAll("import .*?;", "");
    }

    private ProjectInfo findMatchingProjectType(Path path, List<ProjectInfo> projectInfoList) {
        log.info("::::3. findMatchingProjectType 시작::::");
        Path parentDir = path.getParent();
        log.info("::::parentDir = " + parentDir);
        for (ProjectInfo projectInfo : projectInfoList) {
            if (parentDir.startsWith(projectInfo.getBasePath())) {
                log.info("::::projectInfo.getBasePath() = " + projectInfo.getBasePath());
                return projectInfo;
            }
        }
        return null;
    }

    private List<ProjectInfo> detectProjectTypeBySubDir(String projectPath) {
        log.info("::::2. 프로젝트 타입 감지 시작::::");
        List<ProjectInfo> projectInfos = new ArrayList<>();
        try {
            log.info("::::Paths.get(projectPath) = {}", Paths.get(projectPath));
            Files.walk(Paths.get(projectPath), 4)
                    .filter(Files::isRegularFile) // 파일만 감지
                    .forEach(dir -> {
                        ProjectInfo projectInfo = projectTypeDetector.detectFramework(dir);
                        if (projectInfo != null) {
                            projectInfos.add(projectInfo);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("프로젝트 하위 디렉토리를 스캔할 수 없습니다.", e);
        }
        return projectInfos;
    }

}
