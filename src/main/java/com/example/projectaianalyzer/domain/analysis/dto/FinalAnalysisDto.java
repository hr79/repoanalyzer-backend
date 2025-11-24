package com.example.projectaianalyzer.domain.analysis.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FinalAnalysisDto {

    private String projectOverview;
    private String architectureSummary;
    private String techStackOverview;

    private List<String> coreFeatures;
    private List<String> keyDesignPatterns;

    private CodeLevelHighlights codeLevelHighlights;
    private SystemCharacteristics systemCharacteristics;

    private List<String> strengthPoints;
    private List<String> recommendations;

    private String futureGrowthDirection;

    @Getter
    @Setter
    public static class CodeLevelHighlights {
        private String caching;
        private String databaseOptimization;
        private String queryOptimization;
        private String transactionManagement;
        private String asyncOrEventFeatures;
        private String securityMechanisms;
        private String testingStrategy;
    }

    @Getter
    @Setter
    public static class SystemCharacteristics {
        private String projectStructure;
        private String codeQuality;
        private String maintainability;
        private String scalability;
        private String performance;
        private String security;
        private String dataModel;
        private String integrationPoints;
        private String devOps;
    }
}
