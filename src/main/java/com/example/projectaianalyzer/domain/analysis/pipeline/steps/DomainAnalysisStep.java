package com.example.projectaianalyzer.domain.analysis.pipeline.steps;

import com.example.projectaianalyzer.domain.analysis.pipeline.steps.DomainAnalysisStep.DomainAnalysisInput;
import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.dto.FinalAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.service.AnalysisManager;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainAnalysisStep implements AnalysisStep<DomainAnalysisInput, FinalAnalysisDto>{

    private final AnalysisManager analysisManager;

    @Override
    public FinalAnalysisDto execute(DomainAnalysisInput domainAnalysisInput) {
        return analysisManager.analyzeByFileStructureResult(
                domainAnalysisInput.fileStructureAnalysisDtoList(),
                domainAnalysisInput.fileInfoList(),
                domainAnalysisInput.projectPath()
        );
    }

    public record DomainAnalysisInput(List<FileStructureAnalysisDto> fileStructureAnalysisDtoList, List<FileInfo> fileInfoList, String projectPath) {
    }
}

