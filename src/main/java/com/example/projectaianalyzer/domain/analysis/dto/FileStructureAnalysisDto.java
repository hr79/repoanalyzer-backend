package com.example.projectaianalyzer.domain.analysis.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FileStructureAnalysisDto {
    private String domain;
    private List<String> files;
    private String priority;
    private String reason;


    public FileStructureAnalysisDto(String domain, List<String> files, String priority, String reason) {
        this.domain = domain;
        this.files = files;
        this.priority = priority;
        this.reason = reason;
    }
}
