package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import com.example.projectaianalyzer.infra.util.ResultCleaner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class DomainTaskRunner {

    private final DomainAnalysisService domainAnalysisService;
    private final ExecutorService groqExecutor;

    public DomainTaskRunner(DomainAnalysisService domainAnalysisService, @Qualifier("groqExecutor") ExecutorService groqExecutor) {
        this.domainAnalysisService = domainAnalysisService;
        this.groqExecutor = groqExecutor;
    }

    public String runAnalyzeDomain(FileStructureAnalysisDto fileStructureAnalysisDto, Map<String, FileInfo> fileInfoMap) {
        String domain = fileStructureAnalysisDto.getDomain();
        String priority = fileStructureAnalysisDto.getPriority();
        List<String> filesByPriority = fileStructureAnalysisDto.getFiles();

        if (filesByPriority == null || filesByPriority.isEmpty()) {
            log.warn(priority + " 중요도의 " + domain + " 도메인 파일이 없습니다.");
            return null;
        }

        Map<FileRole, List<FileInfo>> filesByRole = new HashMap<>();

        switch (priority) {
            case "critical", "significant", "high", "medium-high", "medium" -> {
                filesByRole = mapDomainFilesByRole(filesByPriority, fileInfoMap);
            }
            case "low" -> {
                return null;
            }
        }
        if (filesByRole.isEmpty()) {
            throw new IllegalArgumentException("groupedByRole is empty.");
        }

        List<CompletableFuture<String>> futureList = new ArrayList<>();

        filesByRole.forEach((role, files) -> {
            if (files == null || files.isEmpty()) {
                log.info(":::: {} 중요도/ {} 도메인의 {} 레이어 파일이 없습니다.", priority, domain, role);
                return;
            }
//                    if (files != null && !files.isEmpty()) {
            log.info(":::: {} 중요도/ {} 도메인의 {} 레이어 분석을 시작합니다.", priority, domain, role);
            CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(
                            () -> domainAnalysisService.analyzeByRole(priority, role, files), groqExecutor)
                    .thenApply(r -> ResultCleaner.getCleanResult(r))
                    .exceptionally(ex -> {
                        log.error(":::: {} 도메인의 {} 레이어 분석 실패", domain, role, ex);
                        return null;
                    });
            futureList.add(stringCompletableFuture);
        });

        if (futureList.isEmpty()) {
            log.warn("{} 중요도의 {} 도메인을 최종 분석할 리스트가 없습니다", priority, domain);
            return null;
        }

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();

        List<String> roleAnalysisResults = futureList.stream()
                .map(f -> f.getNow(null))
                .filter(Objects::nonNull)
                .toList();

        log.info(":::: {} 도메인의 {} 레이어 분석 완료. 이제 도메인 통합 분석 시작", priority, domain);

        if (roleAnalysisResults.isEmpty()) {
            log.warn("{} 중요도의 {} 도메인 분석 결과가 모두 실패했습니다", priority, domain);
            return null;
        }

        log.info(":::: {} 도메인의 도메인 통합 분석 중...", domain);
        String finalResult = domainAnalysisService.analyzeDomain(priority, domain, roleAnalysisResults);
        finalResult = ResultCleaner.getCleanResult(finalResult);
        log.info("domain: {}, priority: {}, result: {}", domain, priority, finalResult);

        return finalResult;
    }

    private Map<FileRole, List<FileInfo>> mapDomainFilesByRole(
            List<String> filesByPriority,
            Map<String, FileInfo> fileInfoMap
    ) {
        log.info(":::: mapDomainFilesByRole ::::");
        if (filesByPriority == null || filesByPriority.isEmpty()) {
            return null;
        }

        Map<FileRole, List<FileInfo>> filesByRole = new HashMap<>();

        log.info("role에 따라 파일 분류를 시작합니다.");
        // role마다 빈 리스트 생성해서 key FileRole, value List로 넣기
        for (FileRole role : FileRole.values()) {
            filesByRole.put(role, new ArrayList<>());
        }

        // 구조분석결과 파일들과 fileInfoMap 매칭해서 fileInfo찾기
        // -> 찾은 fileInfo로 role에 맞게 groupedByRole에 fileInfo 추가
        for (String filePath : filesByPriority) {
            FileInfo fileInfo = fileInfoMap.get(filePath);
            if (fileInfo == null) {
                continue;
            }

            String role = fileInfo.getRole();
//            log.info(":::: role: {}", role);
            FileRole fileRole = FileRole.from(role);
            if (fileRole == null) {
                continue;
            }
            List<FileInfo> fileInfos = filesByRole.get(fileRole);
            fileInfos.add(fileInfo);
        }
        return filesByRole;
    }
}
