package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import com.example.projectaianalyzer.infra.util.ResultCleaner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainTaskRunner {

    private final DomainAnalysisService domainAnalysisService;

    public String runAnalyzeDomain(FileStructureAnalysisDto fileStructureAnalysisDto, Map<String, FileInfo> fileInfoMap) {
//        long start = System.nanoTime();
//        try {
                String domain = fileStructureAnalysisDto.getDomain();
                String priority = fileStructureAnalysisDto.getPriority();
                List<String> filesByPriority = fileStructureAnalysisDto.getFiles();

                if (filesByPriority == null || filesByPriority.isEmpty()) {
                    log.warn(priority + " 중요도의 " + domain + " 도메인 파일이 없습니다.");
                    return null;
                }

                Map<FileRole, List<FileInfo>> groupedByRole = new HashMap<>();

                switch (priority) {
                    case "significant", "high", "medium" -> {
                         groupedByRole = mapDomainFilesByRole(filesByPriority, fileInfoMap);
                    }
                    case "low" -> {
                        return null;
                    }
                }
                if (groupedByRole.isEmpty()) {
                    throw new IllegalArgumentException("groupedByRole is empty.");
                }

                List<String> domainLayerAnalysis = new ArrayList<>();

                groupedByRole.forEach((role, files) -> {
                    if (files != null && !files.isEmpty()) {
                        log.info(":::: {} 중요도/ {} 도메인의 {} 레이어 분석을 시작합니다.", priority, domain, role);
                        String result = domainAnalysisService.analyzeByRole(priority, role, files);
                        result = ResultCleaner.getCleanResult(result);
                        domainLayerAnalysis.add(result);
                    } else {
                        log.info(":::: {} 중요도/ {} 도메인의 {} 레이어 파일이 없습니다.", priority, domain, role);
                    }
                });

                if (domainLayerAnalysis.isEmpty()) {
                    return null;
                }
                log.info(":::: {} 중요도의 {} 도메인 분석을 시작합니다.", priority, domain);
                String priorityAnalysis = domainAnalysisService.analyzeDomainByPriority(priority, domain, domainLayerAnalysis);
                priorityAnalysis = ResultCleaner.getCleanResult(priorityAnalysis);
                System.out.println(priorityAnalysis);

                return priorityAnalysis;

//        } finally {
//            long end = System.nanoTime();
//            long elapsedTime = end - start;
//
//            log.info("runAnalyzeDomains 실행시간: {} ns", elapsedTime);
//            log.info("runAnalyzeDomains 실행시간: {} ms", elapsedTime / 1_000_000);
//            log.info("runAnalyzeDomains 실행시간: {} s", elapsedTime / 1_000_000_000);
//        }
    }

    private Map<FileRole, List<FileInfo>> mapDomainFilesByRole(List<String> filesByPriority,
                                      Map<String, FileInfo> fileInfoMap
                                      ) {
        log.info(":::: mapDomainFilesByRole ::::");
        if (filesByPriority == null || filesByPriority.isEmpty()) {
            return null;
        }

        Map<FileRole, List<FileInfo>> groupedByRole = new HashMap<>();

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
        return groupedByRole;
    }
}
