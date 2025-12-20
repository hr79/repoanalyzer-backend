package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainConcurrentProcessor {
    private final ExecutorService executor;
    private final DomainTaskRunner domainTaskRunner;

    public List<String> analyzeDomains(List<FileStructureAnalysisDto> fileStructureDtoList, Map<String, FileInfo> fileInfoMap) {
        long start = System.nanoTime();
        try {
            List<CompletableFuture<String>> futures = fileStructureDtoList.stream()
                    .map(dto -> CompletableFuture.supplyAsync(
                            () -> domainTaskRunner.runAnalyzeDomain(dto, fileInfoMap),
                            executor
                    ).handle((res, ex) -> {
                        if (ex != null) {
                            log.error("Domain analysis failed: {}", dto.getDomain(), ex);
                            return null;
                        }
                        return res;
                    })) // 예외가 존재하면 null, 존재하지않으면 res 반환
                    .toList();

            // 모든 작업들이 완료할때까지 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .filter(result -> result != null)
                    .toList();
        } finally {
            long end = System.nanoTime();
            long elapsedTime = end - start;

            log.info("analyzeDomains 멀티스레딩 처리완료 시간: {} ms", elapsedTime / 1_000_000);
            log.info("analyzeDomains 멀티스레딩 처리완료 시간: {} s", elapsedTime / 1_000_000_000);
        }
    }
}
