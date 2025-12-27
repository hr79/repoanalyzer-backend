package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.ai.service.GroqClient;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import com.example.projectaianalyzer.domain.ai.model.GroqAiModel;
import com.example.projectaianalyzer.common.PromptRegistry;
import com.example.projectaianalyzer.infra.util.CodeProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainAnalysisServiceImpl implements DomainAnalysisService {
    private final GroqClient groqClient;
    private final CodeProvider codeProvider;

    /**
     * 도메인(예: controller/service 등)에 대한 분석을 수행합니다.
     * 반환값은 각 배치별 Groq 응답 문자열의 리스트입니다.
     */
    public String analyzeByRole(String priority, FileRole role, List<FileInfo> files) {
        log.info(":::: analyzeByRole 시작 - priority: {}, role: {}, 파일 수: {}, 스레드: {}",
                priority, role.getName(), files.size(), Thread.currentThread().getName());

        List<Map<String, String>> filesAsJson = new ArrayList<>();

        files.forEach(f -> {
            String content = codeProvider.loadContent(f.getAbsolutePath());
            filesAsJson.add(
                    Map.of("fileName", f.getFileName(),
                            "path", f.getRelativePath(),
                            "role", role.getName(),
                            "code", content));
        });

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(filesAsJson);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        PriorityDomainPromptAndGroqModel priorityStrategy = PriorityDomainPromptAndGroqModel.from(priority);
        String defaultModel = priorityStrategy.defaultModel();
        String fallbackModel = priorityStrategy.fallbackModel();

        String userMessage = PromptRegistry.DOMAIN_ROLE_ANALYSIS_USER_MESSAGE_PROMPT + jsonString;

        log.info(":::: analyzeByRole - Groq API 호출 직전, role: {}", role.getName());

        String result = groqClient.analyzeProject(
                filesAsJson,
                Map.of("role", "system", "content", PromptRegistry.DOMAIN_ROLE_ANALYSIS_SYSTEM_PROMPT),
                Map.of("role", "user", "content", userMessage),
                defaultModel,
                fallbackModel);

        log.info(":::: analyzeByRole 완료 - priority: {}, role: {}, 스레드: {}",
                priority, role.getName(), Thread.currentThread().getName());

        return result;
    }

    public String analyzeDomain(String priority, String domain, List<String> resultsByRoles) {
        log.info(":::: analyzeDomainsByPriority ::::");
        if (resultsByRoles == null || resultsByRoles.isEmpty()) {
            throw new IllegalArgumentException("files is empty");
        }

        PriorityDomainPromptAndGroqModel promptAndGroqModel = PriorityDomainPromptAndGroqModel.from(priority);
        String systemMessage = promptAndGroqModel.systemMessage();
        String userMessagePrompt = promptAndGroqModel.userPrompt();
        String defaultModel = promptAndGroqModel.defaultModel();
        String fallbackModel = promptAndGroqModel.fallbackModel();

        String userMessage = "The domain is '" + domain + "'. " + userMessagePrompt + String.join("\n", resultsByRoles);

        return groqClient.analyzeProject(
                resultsByRoles,
                Map.of("role", "system", "content", systemMessage),
                Map.of("role", "user", "content", userMessage),
                defaultModel,
                fallbackModel
        );
    }

    public String analyzeFinalByAllResults(List<String> resultsByDomain) {
        log.info(":::: analyzeEntireProjectByAllDomains ::::");
        if (resultsByDomain == null || resultsByDomain.isEmpty()) {
            throw new IllegalArgumentException("files is empty");
        }
        String userMessage = PromptRegistry.FINAL_USER_MESSAGE_PROMPT + String.join("\n", resultsByDomain);
        return groqClient.analyzeProject(
                resultsByDomain,
                Map.of("role", "system", "content", PromptRegistry.FINAL_SYSTEM_MESSAGE_PROMPT),
                Map.of("role", "user", "content", userMessage),
                GroqAiModel.ENTIRE_PROJECT_MAIN.getModelVersion(),
                GroqAiModel.ENTIRE_PROJECT_SUB.getModelVersion()
        );
    }
}
