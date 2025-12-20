package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.common.PromptRegistry;
import com.example.projectaianalyzer.domain.ai.model.GroqAiModel;

public enum PriorityDomainPromptAndGroqModel {

    CRITICAL(
            PromptRegistry.CRITICAL_DOMAIN_ANALYSIS_SYSTEM_PROMPT,
            PromptRegistry.CRITICAL_DOMAIN_ANALYSIS_USER_PROMPT,
            GroqAiModel.CRITICAL_MAIN.getModelVersion(),
            GroqAiModel.CRITICAL_SUB.getModelVersion()
    ),
    SIGNIFICANT(
            PromptRegistry.SIGNIFICANT_PRIORITY_DOMAIN_SYSTEM_MESSAGE_PROMPT,
            PromptRegistry.SIGNIFICANT_PRIORITY_DOMAIN_USER_MESSAGE_PROMPT,
            GroqAiModel.SIGNIFICANT_MAIN.getModelVersion(),
            GroqAiModel.SIGNIFICANT_SUB.getModelVersion()
    ),
    HIGH(
            PromptRegistry.HIGH_PRIORITY_DOMAIN_SYSTEM_MESSAGE_PROMPT,
            PromptRegistry.HIGH_PRIORITY_DOMAIN_USER_MESSAGE_PROMPT,
            GroqAiModel.HIGH_MAIN.getModelVersion(),
            GroqAiModel.HIGH_SUB.getModelVersion()
    ),
    MEDIUM_HIGH(PromptRegistry.MEDIUM_HIGH_DOMAIN_ANALYSIS_SYSTEM_PROMPT,
            PromptRegistry.MEDIUM_HIGH_DOMAIN_ANALYSIS_USER_PROMPT,
            GroqAiModel.MEDIUM_HIGH_MAIN.getModelVersion(),
            GroqAiModel.MEDIUM_HIGH_SUB.getModelVersion()
    ),
    MEDIUM(
            PromptRegistry.MEDIUM_PRIORITY_DOMAIN_SYSTEM_MESSAGE_PROMPT,
            PromptRegistry.MEDIUM_PRIORITY_DOAMIN_USER_MESSAGE_PROMPT,
            GroqAiModel.MEDIUM_MAIN.getModelVersion(),
            GroqAiModel.MEDIUM_SUB.getModelVersion()
    );

    private final String systemMessage;
    private final String userPrompt;
    private final String defaultModel;
    private final String fallbackModel;

    PriorityDomainPromptAndGroqModel(String systemMessage, String userPrompt, String defaultModel, String fallbackModel) {
        this.systemMessage = systemMessage;
        this.userPrompt = userPrompt;
        this.defaultModel = defaultModel;
        this.fallbackModel = fallbackModel;
    }

    public String systemMessage() {
        return systemMessage;
    }

    public String userPrompt() {
        return userPrompt;
    }

    public String defaultModel() {
        return defaultModel;
    }

    public String fallbackModel() {
        return fallbackModel;
    }

    public static PriorityDomainPromptAndGroqModel from(String priority) {
        return switch (priority) {
            case "critical" -> CRITICAL;
            case "significant" -> SIGNIFICANT;
            case "high" -> HIGH;
            case "medium-high" -> MEDIUM_HIGH;
            case "medium" -> MEDIUM;
            default -> throw new IllegalArgumentException("Unknown priority: " + priority);
        };
    }
}