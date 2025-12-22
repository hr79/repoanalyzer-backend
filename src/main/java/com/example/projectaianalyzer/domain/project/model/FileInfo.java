package com.example.projectaianalyzer.domain.project.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FileInfo {
    private String fileName;
    private String relativePath;
    private String absolutePath;
    private String extension; // 확장자
    private String role;
    private ProjectInfo projectInfo;

    @Builder
    public FileInfo(String fileName, String relativePath, String absolutePath, String extension, String role, ProjectInfo projectInfo) {
        this.fileName = fileName;
        this.relativePath = relativePath;
        this.absolutePath = absolutePath;
        this.extension = extension;
        this.role = role;
        this.projectInfo = projectInfo;
    }
}
