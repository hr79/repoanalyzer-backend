package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.ai.model.GroqAiModel;
import lombok.Getter;

@Getter
public enum PriorityStrategy {
    SIGNIFICANT(
            GroqAiModel.SIGNIFICANT_MAIN.getModelVersion(),
            GroqAiModel.SIGNIFICANT_SUB.getModelVersion()
    ),
    HIGH(
            GroqAiModel.HIGH_MAIN.getModelVersion(),
            GroqAiModel.HIGH_SUB.getModelVersion()
    ),
    MEDIUM(
            GroqAiModel.MEDIUM_MAIN.getModelVersion(),
            GroqAiModel.MEDIUM_SUB.getModelVersion()
    );

    private final String defaultModel;
    private final String fallbackModel;

    PriorityStrategy(String defaultModel, String fallbackModel) {
        this.defaultModel = defaultModel;
        this.fallbackModel = fallbackModel;
    }

    public static PriorityStrategy from(String priority) {
        return switch (priority) {
            case "significant" -> SIGNIFICANT;
            case "high" -> HIGH;
            case "medium" -> MEDIUM;
            default -> throw new IllegalArgumentException("Unknown priority: " + priority);
        };
    }
}