package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.dto.FinalAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.service.AnalyzeServiceInterface.AnalysisManager;
import com.example.projectaianalyzer.domain.analysis.service.AnalyzeServiceInterface.FileStructureAnalyzeService;
import com.example.projectaianalyzer.domain.analysis.service.AnalyzeServiceInterface.GitService;
import com.example.projectaianalyzer.domain.analysis.service.AnalyzeServiceInterface.ProjectFileScanner;
import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import com.example.projectaianalyzer.infra.util.FileInfoJsonWriter;
import com.example.projectaianalyzer.infra.util.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzeService {

    private final ProjectFileScanner projectFileScanner;
    private final FileStructureAnalyzeService fileStructureAnalyzeService;
    private final AnalysisManager analysisManager;
    private final GitService gitService;
    private final JsonParser jsonParser;

    private final static String BASE_PATH = "tmp/repo/";
    private final static String TEMP_ROOT_FOLDER = "hr79/post-it";

    public FinalAnalysisDto analyze(String repoUrl) {
        // 1) 프로젝트 저장
        String projectRootFolderName = repoUrl.replace("https://github.com/", "")
                .replace(".git", "");
        log.info("projectRootFolderName: {}", projectRootFolderName);
        String projectPath = BASE_PATH + projectRootFolderName; // 현재 프로젝트 루트 기준 경로
        log.info("projectPath: {}", projectPath);

        gitService.cloneRepository(repoUrl, projectPath);

        List<FileInfo> businessLogicFiles = new ArrayList<>();
        List<String> fileStructureSummaries = new ArrayList<>();

        // 2) 프로젝트 파일 스캔 및 비즈니스 로직 파일 식별 후 코드 정보 수집
        List<FileInfo> fileInfoList = projectFileScanner.scanProjectDirectory(projectPath, businessLogicFiles, fileStructureSummaries);

        // 3) 검사용 JSON 덤프
        FileInfoJsonWriter.writeToJsonFile(fileInfoList, projectPath + ".json");

        // 4) 파일 경로 정보로 가장 먼저 도메인/핵심기능 분석 요청
        String fileStructureAnalysis = fileStructureAnalyzeService.analyzeFileStructure(fileStructureSummaries);

        log.info("fileStructureAnalysis : {}",fileStructureAnalysis);

        List<FileStructureAnalysisDto> fileStructureAnalysisDtoList = jsonParser.parseJson(fileStructureAnalysis, new TypeReference<List<FileStructureAnalysisDto>>() {});

        if (fileStructureAnalysisDtoList == null || fileStructureAnalysisDtoList.isEmpty()) {
            throw new IllegalArgumentException("files is empty");
        }

        log.info(":::: 도메인 중요도 분석 결과를 json 파일로 만듭니다.");
        // 도메인 중요도 분석 결과 json파일 저장
        FileInfoJsonWriter.writeToJsonFile(fileStructureAnalysis, projectPath + "/domainAnalysis.json");

        return analysisManager.analyzeByFileStructureResult(fileStructureAnalysisDtoList, fileInfoList, projectPath);

//        try {
//            deleteDirectory(Paths.get(projectPath));
//            log.info("Successfully deleted project directory: {}", projectPath);
//        } catch (IOException e) {
//            log.error("Failed to delete project directory: {}", projectPath, e);
//        }

    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete: " + p, e);
                        }
                    });
        }
    }
}
