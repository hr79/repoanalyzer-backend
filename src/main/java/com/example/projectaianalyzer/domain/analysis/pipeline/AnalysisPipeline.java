package com.example.projectaianalyzer.domain.analysis.pipeline;

import com.example.projectaianalyzer.domain.analysis.pipeline.steps.AnalyzeFileStructureStep;
import com.example.projectaianalyzer.domain.analysis.pipeline.steps.DomainAnalysisStep;
import com.example.projectaianalyzer.domain.analysis.pipeline.steps.DomainAnalysisStep.DomainAnalysisInput;
import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.dto.FinalAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.pipeline.steps.GitCloneStep;
import com.example.projectaianalyzer.domain.analysis.pipeline.steps.ScanProjectStep;
import com.example.projectaianalyzer.domain.project.service.FileScannerResult;
import com.example.projectaianalyzer.infra.util.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalysisPipeline {

    private final GitCloneStep gitCloneStep;
    private final ScanProjectStep scanProjectStep;
    private final AnalyzeFileStructureStep analyzeFileStructureStep;
    private final DomainAnalysisStep domainAnalysisStep;
    private final FileStorage fileStorage;

    public FinalAnalysisDto run(String repoUrl) {
        String path = gitCloneStep.execute(repoUrl);
        FileScannerResult fileScannerResult = scanProjectStep.execute(path);
        List<String> fileStructureSummaries = fileScannerResult.fileStructureSummaries();
        List<FileStructureAnalysisDto> fileStructureAnalysisResults = analyzeFileStructureStep.execute(fileStructureSummaries);
        FinalAnalysisDto finalAnalysisDto = domainAnalysisStep.execute(
                new DomainAnalysisInput(
                        fileStructureAnalysisResults,
                        fileScannerResult.fileInfoList(),
                        path
                )
        );
        fileStorage.delete(path);

        return finalAnalysisDto;
    }
}
