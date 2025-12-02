package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.dto.FinalAnalysisDto;
import com.example.projectaianalyzer.domain.project.model.FileInfo;

import java.util.List;

public interface AnalysisManager {
    FinalAnalysisDto analyzeByFileStructureResult(List<FileStructureAnalysisDto> fileStructureAnalysisDtoList, List<FileInfo> businessLogicFiles, String projectPath);
}
