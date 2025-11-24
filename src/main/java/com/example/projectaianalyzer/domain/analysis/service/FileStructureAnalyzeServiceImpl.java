package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.common.PromptRegistry;
import com.example.projectaianalyzer.domain.ai.model.GroqAiModel;
import com.example.projectaianalyzer.domain.ai.service.GroqClient;
import com.example.projectaianalyzer.domain.analysis.service.AnalyzeServiceInterface.FileStructureAnalyzeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileStructureAnalyzeServiceImpl implements FileStructureAnalyzeService {
    private final GroqClient groqClient;

    @Override
    public String analyzeFileStructure(List<String> fileStructureSummaries) {
        log.info(":::: analyzeFileStructure ::::");
        return groqClient.analyzeProject(
                fileStructureSummaries,
                Map.of("role", "system", "content", PromptRegistry.FILE_STRUCTURE_ANALYSIS_SYSTEM_PROMPT),
                Map.of("role", "user", "content", PromptRegistry.FILE_STRUCTURE_ANALYSIS_USER_PROMPT + fileStructureSummaries),
                GroqAiModel.STRUCTURE_AND_DOMAIN_MAIN.getModelVersion(),
                GroqAiModel.STRUCTURE_AND_DOMAIN_SUB.getModelVersion()
        );
    }
}
