package com.example.projectaianalyzer.domain.project.service;

import com.example.projectaianalyzer.domain.project.model.FileInfo;
import com.example.projectaianalyzer.domain.project.model.ProjectInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectFileScannerImpl implements ProjectFileScanner {
    private final ProjectTypeDetector projectTypeDetector;
    private final FileMetadataExtractor fileMetadataExtractor;
    private final ProjectWalker projectWalker;

    @Override
    public FileScannerResult scanProjectDirectory(String filePath) {
        log.info("::::1. 프로젝트 폴더 스캔 시작::::");
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("경로 정보가 없습니다.");
        }

        Path rootPath = Paths.get(filePath);
        if (!Files.exists(rootPath)) {
            throw new IllegalArgumentException("스캔할 프로젝트 경로가 유효하지 않습니다.");
        }

        // 하위 디렉토리 탐색하며 backend, frontend, language, framework 감지
        List<ProjectInfo> projectInfoList = detectProjectTypeBySubDir(filePath);
        List<FileInfo> fileInfoList = new ArrayList<>();
        List<String> fileStructureSummaries = new ArrayList<>();

        projectWalker.walkFiles(rootPath)
                .forEach(path -> {
                    log.debug("::::path = {}", path);
                    ProjectInfo projectInfo = findMatchingProjectType(path, projectInfoList);
                    FileInfo fileInfo = fileMetadataExtractor.extract(path, rootPath, projectInfo);
                    if (fileInfo == null) {
                        return;
                    }
                    // 파일구조 요약에 필요한 리스트에 상대경로 정보 추가
                    fileStructureSummaries.add(fileInfo.getRelativePath());
                    fileInfoList.add(fileInfo);
                });
        return new FileScannerResult(fileInfoList, fileStructureSummaries);
    }

    private ProjectInfo findMatchingProjectType(Path path, List<ProjectInfo> projectInfoList) {
        log.debug("::::3. findMatchingProjectType 시작::::");
        Path parentDir = path.getParent();
        for (ProjectInfo projectInfo : projectInfoList) {
            if (parentDir != null && parentDir.startsWith(projectInfo.getBasePath())) {
                return projectInfo;
            }
        }
        return null;
    }

    private List<ProjectInfo> detectProjectTypeBySubDir(String projectPath) {
        log.debug("::::2. 프로젝트 프레임워크 감지 시작::::");
        List<ProjectInfo> projectInfos = new ArrayList<>();

        projectWalker.walkFiles(Paths.get(projectPath), 4)
                .forEach(dir -> {
                    ProjectInfo projectInfo = projectTypeDetector.detectFramework(dir);
                    if (projectInfo != null) {
                        projectInfos.add(projectInfo);
                    }
                });
        return projectInfos;
    }

}
