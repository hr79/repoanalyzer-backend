package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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
                    .thenCompose(f -> f)  // CompletableFuture<String>을 CompletableFuture<String>로 변환
                    .exceptionally(ex -> {
                        log.error("Domain analysis failed: {}", dto.getDomain(), ex);
                        return "";
                    })
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info(":::: {} 도메인 분석 완료", dto.getDomain());
                        } else {
                            log.error(":::: {} 도메인 분석 실패: {}", dto.getDomain(), ex.getMessage());
                        }
                    });
                }).toList();

            // 수정: join() 호출 시 모든 CompletableFuture가 이미 완료된 상태
            // thenCompose()를 통해 모든 futures가 순차적으로 완료되도록 보장
            log.info(":::: 총 {} 개의 도메인 분석 완료 대기 중", futures.size());

            return futures.stream()
                .map(CompletableFuture::join)
                .filter(s -> s != null && !s.isBlank())
                .toList();
        } finally {
            long end = System.nanoTime();
            long elapsedTime = end - start;

            log.info("analyzeDomains 멀티스레딩 처리완료 시간: {} ms", elapsedTime / 1_000_000);
            log.info("analyzeDomains 멀티스레딩 처리완료 시간: {} s", elapsedTime / 1_000_000_000);
        }
    }
}
