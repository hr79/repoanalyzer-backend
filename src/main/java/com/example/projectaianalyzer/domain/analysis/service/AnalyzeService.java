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
        return pipeline.run(repoUrl);
    }
}
