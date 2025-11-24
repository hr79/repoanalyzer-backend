package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.dto.FinalAnalysisDto;
import com.example.projectaianalyzer.domain.project.model.FileInfo;

import java.util.List;

public class AnalyzeServiceInterface {
    public interface GitService {
        void cloneRepository(String repoUrl, String filePath);
    }

    public interface ProjectFileScanner {
        List<FileInfo> scanProjectDirectory(String filePath, List<FileInfo> businessLogicFiles, List<String> fileStructureSummaries);
    }

    public interface FileStructureAnalyzeService {
        String analyzeFileStructure(List<String> fileStructureSummaries);
    }

    public interface AnalysisManager {
        FinalAnalysisDto analyzeByFileStructureResult(List<FileStructureAnalysisDto> fileStructureAnalysisDtoList, List<FileInfo> businessLogicFiles, String projectPath);

    }



}
