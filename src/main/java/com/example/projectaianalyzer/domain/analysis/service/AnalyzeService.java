package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.dto.FinalAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import com.example.projectaianalyzer.domain.project.service.FileScannerResult;
import com.example.projectaianalyzer.domain.project.service.GitServiceImpl;
import com.example.projectaianalyzer.domain.project.service.ProjectFileScanner;
import com.example.projectaianalyzer.infra.util.FileStorage;
import com.example.projectaianalyzer.infra.util.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzeService {

    private final ProjectFileScanner projectFileScanner;
    private final FileStructureAnalyzeService fileStructureAnalyzeService;
    private final AnalysisManager analysisManager;
    private final GitServiceImpl gitService;
    private final JsonParser jsonParser;
    private final FileStorage fileStorage;

    private final static String BASE_PATH = "tmp/repo/";
    private final static String ROOT_PATH = "tmp/";

    public FinalAnalysisDto analyze(String repoUrl) {
        if (repoUrl == null || repoUrl.isBlank()) {
            throw new IllegalArgumentException("repoUrl is required");
        }

        String projectRootFolderName = repoUrl.replace("https://github.com/", "")
                .replace(".git", "");
        log.info("projectRootFolderName: {}", projectRootFolderName);
        String projectPath = BASE_PATH + projectRootFolderName; // 현재 프로젝트 루트 기준 경로
        log.info("projectPath: {}", projectPath);

        gitService.cloneRepository(repoUrl, projectPath);

        // 2) 프로젝트 파일 스캔 및 비즈니스 로직 파일 식별 후 코드 정보 수집
        FileScannerResult scannerResult = projectFileScanner.scanProjectDirectory(projectPath);
        List<FileInfo> fileInfoList = scannerResult.fileInfoList();
        List<String> fileStructureSummaries = scannerResult.fileStructureSummaries();

        // 3) 검사용 JSON 덤프
//        fileStorage.writeJson(fileInfoList, ROOT_PATH + projectRootFolderName + "_file_info.json");

        // 4) 파일 경로 정보로 가장 먼저 도메인/핵심기능 분석 요청
        String fileStructureAnalysis = fileStructureAnalyzeService.analyzeFileStructure(fileStructureSummaries);

        log.info("fileStructureAnalysis : {}", fileStructureAnalysis);

        List<FileStructureAnalysisDto> fileStructureAnalysisDtoList = jsonParser.parseJson(fileStructureAnalysis, new TypeReference<List<FileStructureAnalysisDto>>() {
        });

        if (fileStructureAnalysisDtoList == null || fileStructureAnalysisDtoList.isEmpty()) {
            throw new IllegalArgumentException("files is empty");
        }

        log.info(":::: 도메인 중요도 분석 결과를 json 파일로 만듭니다.");
        // 도메인 중요도 분석 결과 json파일 저장
//        fileStorage.writeJson(fileStructureAnalysis, ROOT_PATH + projectRootFolderName + "_domainAnalysis.json");

        FinalAnalysisDto finalAnalysisDto = analysisManager.analyzeByFileStructureResult(fileStructureAnalysisDtoList, fileInfoList, projectPath);

        fileStorage.delete(projectPath);

        return finalAnalysisDto;
    }
}
