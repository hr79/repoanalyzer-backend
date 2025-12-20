package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.dto.FinalAnalysisDto;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import com.example.projectaianalyzer.infra.util.FileStorage;
import com.example.projectaianalyzer.infra.util.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisManagerImpl implements AnalysisManager {

    private final DomainAnalysisService domainAnalysisService;
    private final JsonParser jsonParser;
    private final FileStorage fileStorage;
    private final DomainConcurrentProcessor domainConcurrentProcessor;

    private final static String ROOT_PATH = "tmp/";

    @Override
    public FinalAnalysisDto analyzeByFileStructureResult(
            List<FileStructureAnalysisDto> fileStructureAnalysisDtoList,
            List<FileInfo> fileInfoList,
            String projectPath
    ) {
        // 구조분석 결과로 나온 파일들과 빠르게 매칭하기 위해 전체 파일 정보 fileinfolist를 map으로 구성
        if (fileInfoList == null || fileInfoList.isEmpty()) {
            throw new IllegalArgumentException("전체 파일 정보들이 없습니다.");
        }
        Map<String, FileInfo> fileInfoMap = new HashMap<>();
        for (FileInfo fileInfo : fileInfoList) {
            fileInfoMap.put(fileInfo.getRelativePath(), fileInfo);
        }

        // 도메인 중요도에 따라 작업
        List<String> accumulatedResults = domainConcurrentProcessor.analyzeDomains(fileStructureAnalysisDtoList, fileInfoMap);

        // 최종 도메인 분석
        log.info(":::: 최종 도메인별 분석 결과 종합 ::::");
        String finalResult = domainAnalysisService.analyzeFinalByAllResults(accumulatedResults);
        log.info(finalResult);

        FinalAnalysisDto finalAnalysisDto = jsonParser.parseJson(finalResult, new TypeReference<FinalAnalysisDto>() {
        });
//        fileStorage.writeJson(finalAnalysisDto, ROOT_PATH + "analysis_report_final.json");;
        return finalAnalysisDto;
    }
}
