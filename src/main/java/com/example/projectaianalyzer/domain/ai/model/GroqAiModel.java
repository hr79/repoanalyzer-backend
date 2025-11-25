package com.example.projectaianalyzer.domain.ai.model;

import lombok.Getter;

@Getter
public enum GroqAiModel {
    STRUCTURE_AND_DOMAIN_MAIN(GroqAiModelVersionRegistry.LLAMA_3_1_8B_INSTANT_VERSION),
    STRUCTURE_AND_DOMAIN_SUB(GroqAiModelVersionRegistry.MOONSHOTAI_KIMI_K2_INSTRUCT),
    SIGNIFICANT_MAIN(GroqAiModelVersionRegistry.LLAMA_4_SCOUT_17B_VERSION),
    SIGNIFICANT_SUB_2(GroqAiModelVersionRegistry.LLAMA_3_1_8B_INSTANT_VERSION),
    SIGNIFICANT_SUB(GroqAiModelVersionRegistry.MOONSHOTAI_KIMI_K2_INSTRUCT),
    HIGH_MAIN(GroqAiModelVersionRegistry.MOONSHOTAI_KIMI_K2_INSTRUCT),
    HIGH_SUB(GroqAiModelVersionRegistry.LLAMA_3_1_8B_INSTANT_VERSION),
    MEDIUM_MAIN(GroqAiModelVersionRegistry.LLAMA_3_1_8B_INSTANT_VERSION),
    MEDIUM_SUB(GroqAiModelVersionRegistry.COMPOUND_MINI),
    ENTIRE_PROJECT_MAIN(GroqAiModelVersionRegistry.LLAMA_4_SCOUT_17B_VERSION),
    ENTIRE_PROJECT_SUB(GroqAiModelVersionRegistry.MOONSHOTAI_KIMI_K2_INSTRUCT);

    private final String modelVersion;

    GroqAiModel(String modelVersion) {
        this.modelVersion = modelVersion;
    }
}
