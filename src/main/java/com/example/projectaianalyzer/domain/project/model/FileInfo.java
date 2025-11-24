package com.example.projectaianalyzer.domain.project.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class FileInfo {
    private String fileName;
    private String relativePath;
    private String absolutePath;
    private String extension; // 확장자
    private String role;
    private ProjectInfo projectInfo;
    @Setter private String content; // 코드
    @Setter private String summary; // ai 분석 결과

    @Builder
    public FileInfo(String fileName, String relativePath, String absolutePath, String extension, String role, ProjectInfo projectInfo, String content, String summary) {
        this.fileName = fileName;
        this.relativePath = relativePath;
        this.absolutePath = absolutePath;
        this.extension = extension;
        this.role = role;
        this.projectInfo = projectInfo;
        this.content = content;
        this.summary = summary;
    }
}
