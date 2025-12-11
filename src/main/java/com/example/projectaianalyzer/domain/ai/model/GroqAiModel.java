package com.example.projectaianalyzer.domain.ai.model;

import lombok.Getter;

@Getter
public enum GroqAiModel {
    STRUCTURE_AND_DOMAIN_MAIN(GroqAiModelVersionRegistry.LLAMA_3_3_70B_VERSION),
    STRUCTURE_AND_DOMAIN_SUB(GroqAiModelVersionRegistry.GPT_OSS_20B),
    SIGNIFICANT_MAIN(GroqAiModelVersionRegistry.LLAMA_4_SCOUT_17B_16E_VERSION),
    SIGNIFICANT_SUB(GroqAiModelVersionRegistry.GPT_OSS_120B),
    //    SIGNIFICANT_SUB_2(GroqAiModelVersionRegistry.LLAMA_3_1_8B_INSTANT_VERSION),
    HIGH_MAIN(GroqAiModelVersionRegistry.QWEN3_32B_VERSION),
    HIGH_SUB(GroqAiModelVersionRegistry.MOONSHOTAI_KIMI_K2_INSTRUCT),
    MEDIUM_MAIN(GroqAiModelVersionRegistry.LLAMA_4_MAVERICK_17B_128E),
    MEDIUM_SUB(GroqAiModelVersionRegistry.LLAMA_3_1_8B_INSTANT_VERSION),
    ENTIRE_PROJECT_MAIN(GroqAiModelVersionRegistry.GROQ_COMPOUND_MINI),
    ENTIRE_PROJECT_SUB(GroqAiModelVersionRegistry.MOONSHOTAI_KIMI_K2_INSTRUCT_0905);

    private final String modelVersion;

    GroqAiModel(String modelVersion) {
        this.modelVersion = modelVersion;
    }
}
