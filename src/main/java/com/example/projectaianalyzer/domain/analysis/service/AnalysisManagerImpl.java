package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.dto.FinalAnalysisDto;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import com.example.projectaianalyzer.infra.util.FileStorage;
import com.example.projectaianalyzer.infra.util.FileStorageImpl;
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

    @Override
    public FinalAnalysisDto analyzeByFileStructureResult(
            List<FileStructureAnalysisDto> fileStructureAnalysisDtoList,
            List<FileInfo> fileInfoList,
            String projectPath
    ) {
        // 구조분석 결과로 나온 파일들과 빠르게 매칭하기 위해 fileinfolist를 map으로 구성
        if (fileInfoList == null || fileInfoList.isEmpty()) {
            throw new IllegalArgumentException("전체 파일 정보들이 없습니다.");
        }
        Map<String, FileInfo> fileInfoMap = new HashMap<>();
        for (FileInfo fileInfo : fileInfoList) {
            fileInfoMap.put(fileInfo.getRelativePath(), fileInfo);
        }
        List<String> resultsByDomain = new ArrayList<>(); // 각 도메인들의 분석 결과 저장

        // 도메인 중요도에 따라 작업
        for (FileStructureAnalysisDto fileStructureAnalysisDto : fileStructureAnalysisDtoList) {
            String domain = fileStructureAnalysisDto.getDomain();
            String priority = fileStructureAnalysisDto.getPriority();
            List<String> filesByPriority = fileStructureAnalysisDto.getFiles();
            Map<FileRole, List<FileInfo>> groupedByRole = new HashMap<>();
            List<String> domainLayerAnalysis = new ArrayList<>();

            if (filesByPriority == null || filesByPriority.isEmpty()) {
                throw new IllegalArgumentException(priority + " 중요도의 " + domain + " 도메인 파일이 없습니다.");
            }

            switch (priority) {
                case "significant", "high", "medium" -> {
                    mapDomainFilesByRole(filesByPriority, fileInfoMap, groupedByRole);
                }
                case "low" -> {
                    continue;
                }
            }
            if (groupedByRole.isEmpty()) {
                throw new IllegalArgumentException("groupedByRole is empty.");
            }
            groupedByRole.forEach((role, files) -> {
                if (files != null && !files.isEmpty()) {
                    log.info(":::: {} 중요도/ {} 도메인의 {} 레이어 분석을 시작합니다.", priority, domain, role);
                    String result = domainAnalysisService.analyzeByRole(priority, role, files);
                    result = getCleanResult(result);
                    domainLayerAnalysis.add(result);
                } else {
                    log.info(":::: {} 중요도/ {} 도메인의 {} 레이어 파일이 없습니다.", priority, domain, role);
                }
            });

            if (domainLayerAnalysis.isEmpty()) {
                continue;
            }
            log.info(":::: {} 중요도의 {} 도메인 분석을 시작합니다.", priority, domain);
            String priorityAnalysis = domainAnalysisService.analyzeDomainByPriority(priority, domain, domainLayerAnalysis);
            priorityAnalysis = getCleanResult(priorityAnalysis);
            System.out.println(priorityAnalysis);
            resultsByDomain.add(priorityAnalysis);
        }

        // 최종 도메인 분석
        log.info(":::: 최종 도메인별 분석 결과 종합 ::::");
        String finalResult = domainAnalysisService.analyzeEntireProjectByAllDomains(resultsByDomain);
        log.info(finalResult);

        FinalAnalysisDto finalAnalysisDto = jsonParser.parseJson(finalResult, new TypeReference<FinalAnalysisDto>() {
        });
        fileStorage.writeJson(finalAnalysisDto, projectPath + "/analysis_report_final.json");

        return finalAnalysisDto;
    }

    private static String getCleanResult(String result) {
        return result.replaceAll("(?s)<think>.*?</think>", "")
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
    }

    private void mapDomainFilesByRole(List<String> filesByPriority,
                                      Map<String, FileInfo> fileInfoMap,
                                      Map<FileRole, List<FileInfo>> groupedByRole) {
        log.info(":::: mapDomainFilesByRole ::::");
        log.info("role에 따라 파일 분류를 시작합니다.");
        // role마다 빈 리스트 생성해서 key FileRole, value List로 넣기
        for (FileRole role : FileRole.values()) {
            groupedByRole.put(role, new ArrayList<>());
        }

        // 구조분석결과 파일들과 fileInfoMap 매칭해서 fileInfo찾기
        // -> 찾은 fileInfo로 role에 맞게 groupedByRole에 fileInfo 추가
        for (String filePath : filesByPriority) {
            FileInfo fileInfo = fileInfoMap.get(filePath);
            if (fileInfo == null) {
                continue;
            }

            String role = fileInfo.getRole();
            log.info(":::: role: {}", role);
            FileRole fileRole = FileRole.from(role);
            if (fileRole == null) {
                continue;
            }
            List<FileInfo> fileListByRole = groupedByRole.get(fileRole);
            if (fileListByRole == null) {
                List<FileInfo> roleFileList = new ArrayList<>();
                groupedByRole.put(fileRole, roleFileList);
                roleFileList.add(fileInfo);
                continue;
            }
            fileListByRole.add(fileInfo);
        }
    }
}
