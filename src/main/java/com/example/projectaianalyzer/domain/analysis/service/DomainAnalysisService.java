package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.project.model.FileInfo;

import java.util.List;

public interface DomainAnalysisService {
    String analyzeByRole(String priority, FileRole role, List<FileInfo> files);
    String analyzeDomain(String priority, String domain, List<String> resultsByRoles);
    String analyzeFinalByAllResults(List<String> resultsByDomain);
}
