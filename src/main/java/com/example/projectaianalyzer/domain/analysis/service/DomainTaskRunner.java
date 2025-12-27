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

    public CompletableFuture<String> runAnalyzeDomain(FileStructureAnalysisDto fileStructureAnalysisDto, Map<String, FileInfo> fileInfoMap) {
//        long start = System.nanoTime();
//        try {
        String domain = fileStructureAnalysisDto.getDomain();
        String priority = fileStructureAnalysisDto.getPriority();
        List<String> filesByPriority = fileStructureAnalysisDto.getFiles();

        if (filesByPriority == null || filesByPriority.isEmpty()) {
            log.warn(priority + " 중요도의 " + domain + " 도메인 파일이 없습니다.");
            return CompletableFuture.completedFuture("");
        }

        Map<FileRole, List<FileInfo>> filesByRole = new HashMap<>();

        switch (priority) {
            case "critical", "significant", "high", "medium-high", "medium" -> {
                filesByRole = mapDomainFilesByRole(filesByPriority, fileInfoMap);
            }
            case "low" -> {
                return CompletableFuture.completedFuture("");
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
                    .thenApply(r -> ResultCleaner.getCleanResult(r));
            futureList.add(stringCompletableFuture);
//                    } else {
//                        log.info(":::: {} 중요도/ {} 도메인의 {} 레이어 파일이 없습니다.", priority, domain, role);
//                    }
        });

        if (futureList.isEmpty()) {
            log.warn("{} 중요도의 {} 도메인을 최종 분석할 리스트가 없습니다", priority, domain);
            return CompletableFuture.completedFuture("");
        }

        // 수정: CompletableFuture 체인을 사용하여 analysisExecutor 스레드 블로킹 제거
        // allOf() → thenCompose()를 사용하여 명시적으로 다음 단계를 체인
        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .thenCompose(v -> {
                    // 중요: join()을 thenCompose 콜백 내에서만 호출
                    // (analysisExecutor 스레드가 아닌 다른 스레드에서 실행됨)
                    List<String> results = futureList.stream()
                            .map(CompletableFuture::join)
                            .filter(Objects::nonNull)
                            .toList();

                    log.info(":::: {} 도메인의 {} 레이어 분석 완료. 이제 도메인 통합 분석 시작", priority, domain);

                    // 다음 단계: 도메인 분석 (새로운 CompletableFuture 반환)
                    return CompletableFuture.completedFuture(results);
                })
                .thenApply(results -> {
                    log.info(":::: {} 도메인의 도메인 통합 분석 중...", domain);
                    return domainAnalysisService.analyzeDomain(priority, domain, results);
                })
                .thenApply(finalResult -> {
                    finalResult = ResultCleaner.getCleanResult(finalResult);
                    log.info("domain: {}, priority: {}, result: {}", domain, priority, finalResult);
                    return finalResult;
                });
//        } finally {
//            long end = System.nanoTime();
//            long elapsedTime = end - start;
//
//            log.info("runAnalyzeDomains 실행시간: {} ns", elapsedTime);
//            log.info("runAnalyzeDomains 실행시간: {} ms", elapsedTime / 1_000_000);
//            log.info("runAnalyzeDomains 실행시간: {} s", elapsedTime / 1_000_000_000);
//        }
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
