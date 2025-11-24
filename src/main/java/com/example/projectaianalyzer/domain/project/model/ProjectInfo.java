package com.example.projectaianalyzer.domain.project.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class ProjectInfo {

    private String projectName;
    @Setter private String projectLanguage;
    @Setter private String projectFramework;
    @Setter private String projectType; // frontend, backend, mobile, etc.
    private String basePath; // 루트 디렉토리

    @Builder
    public ProjectInfo(String projectName, String projectLanguage, String projectFramework, String projectType, String basePath) {
        this.projectName = projectName;
        this.projectLanguage = projectLanguage;
        this.projectFramework = projectFramework;
        this.projectType = projectType;
        this.basePath = basePath;
    }
}
