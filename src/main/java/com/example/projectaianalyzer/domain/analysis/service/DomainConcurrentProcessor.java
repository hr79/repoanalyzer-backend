package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class DomainConcurrentProcessor {
    private final ExecutorService analysisExecutor;
    private final DomainTaskRunner domainTaskRunner;

    public DomainConcurrentProcessor(@Qualifier("analysisExecutor") ExecutorService analysisExecutor, DomainTaskRunner domainTaskRunner) {
        this.analysisExecutor = analysisExecutor;
        this.domainTaskRunner = domainTaskRunner;
    }

    public List<String> analyzeDomains(List<FileStructureAnalysisDto> fileStructureDtoList, Map<String, FileInfo> fileInfoMap) {
        long start = System.nanoTime();
        try {
            List<CompletableFuture<String>> futures = fileStructureDtoList.stream()
                    .map(dto -> {
                        log.info(":::: {} 도메인 분석 시작", dto.getDomain());
                        return CompletableFuture.supplyAsync(
                                        () -> domainTaskRunner.runAnalyzeDomain(dto, fileInfoMap),
                                        analysisExecutor
                                )
                                .exceptionally(ex -> {
                                    log.error("Domain analysis failed: {}", dto.getDomain(), ex);
                                    return null;
                                })
                                .whenComplete((result, ex) -> {
                                    if (ex == null) {
                                        log.info(":::: {} 도메인 분석 완료", dto.getDomain());
                                    } else {
                                        log.error(":::: {} 도메인 분석 실패: {}", dto.getDomain(), ex.getMessage());
                                    }
                                });
                    }).toList();

            log.info(":::: 총 {} 개의 도메인 분석 완료 대기 중", futures.size());
            log.info("DomainConcurrentProcessor: current thread = {}", Thread.currentThread().getName());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<String> results = futures.stream()
                    .map(f -> f.getNow(null))
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isBlank())
                    .toList();
            return results;

        } finally {
            long end = System.nanoTime();
            long elapsedTime = end - start;

            log.info("analyzeDomains 멀티스레딩 처리완료 시간: {} ms", elapsedTime / 1_000_000);
            log.info("analyzeDomains 멀티스레딩 처리완료 시간: {} s", elapsedTime / 1_000_000_000);
        }
    }
}
