package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.ai.service.GroqClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStructureAnalyzeServiceImplTest {

    @Mock
    private GroqClient groqClient;

    @InjectMocks
    private FileStructureAnalyzeServiceImpl service;

    @Test
    @DisplayName("Given valid summaries When analyzeFileStructure Then return AI result")
    void Given_ValidSummaries_When_AnalyzeFileStructure_Then_ReturnAiResult() {
        List<String> summaries = List.of("A.java", "B.java");

        when(groqClient.analyzeProject(
                eq(summaries),
                any(Map.class),
                any(Map.class),
                anyString(),
                anyString()
        )).thenReturn("analysis-result");

        String result = service.analyzeFileStructure(summaries);

        assertEquals("analysis-result", result);
        verify(groqClient, times(1)).analyzeProject(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Given empty summaries When analyzeFileStructure Then still call AI")
    void Given_EmptySummaries_When_AnalyzeFileStructure_Then_CallAi() {
        List<String> summaries = List.of();

        when(groqClient.analyzeProject(any(), any(), any(), any(), any()))
                .thenReturn("empty");

        String result = service.analyzeFileStructure(summaries);

        assertEquals("empty", result);
        verify(groqClient, times(1)).analyzeProject(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Given AI throws exception When analyzeFileStructure Then propagate exception")
    void Given_AiThrows_When_AnalyzeFileStructure_Then_Throw() {
        List<String> summaries = List.of("x");

        when(groqClient.analyzeProject(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("AI error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.analyzeFileStructure(summaries));

        assertEquals("AI error", ex.getMessage());
        verify(groqClient).analyzeProject(any(), any(), any(), any(), any());
    }
}