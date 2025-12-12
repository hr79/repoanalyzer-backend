package com.example.projectaianalyzer.domain.analysis.pipeline.steps;

import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.service.FileStructureAnalyzeService;
import com.example.projectaianalyzer.infra.util.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyzeFileStructureStep implements AnalysisStep<List<String>, List<FileStructureAnalysisDto>> {

    private final FileStructureAnalyzeService fileStructureAnalyzeService;
    private final JsonParser jsonParser;

    @Override
    public List<FileStructureAnalysisDto> execute(List<String> fileStructureSummaries) {
        String analysisResult = fileStructureAnalyzeService.analyzeFileStructure(fileStructureSummaries);

        log.info(":::: 파일 구조 분석 결과: {}", analysisResult);

        return jsonParser.parseJson(
                analysisResult,
                new TypeReference<List<FileStructureAnalysisDto>>() {
                });
    }
}
