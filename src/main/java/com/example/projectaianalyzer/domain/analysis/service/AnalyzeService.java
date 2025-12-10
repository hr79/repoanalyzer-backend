package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.pipeline.AnalysisPipeline;
import com.example.projectaianalyzer.domain.analysis.dto.FinalAnalysisDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzeService {

    private final AnalysisPipeline pipeline;

    public FinalAnalysisDto analyze(String repoUrl){
        long start = System.nanoTime();
        try {
            return pipeline.run(repoUrl);
        } finally {
            long end = System.nanoTime();
            long elapsedTime = end - start;

            log.info("순차처리 AnalyzeService 처리시간: {} ms", elapsedTime / 1_000_000);
            log.info("순차처리 AnalyzeService 처리시간: {} s", elapsedTime / 1_000_000_000);
        }
    }
}
