package com.example.projectaianalyzer.domain.analysis.pipeline.steps;

import com.example.projectaianalyzer.domain.project.service.FileScannerResult;
import com.example.projectaianalyzer.domain.project.service.ProjectFileScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScanProjectStep implements AnalysisStep<String, FileScannerResult>{

    private final ProjectFileScanner projectFileScanner;

    @Override
    public FileScannerResult execute(String filePath) {
        return projectFileScanner.scanProjectDirectory(filePath);
    }
}
