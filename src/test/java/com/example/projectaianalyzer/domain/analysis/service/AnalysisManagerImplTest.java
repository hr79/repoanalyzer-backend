package com.example.projectaianalyzer.domain.analysis.service;

import com.example.projectaianalyzer.domain.analysis.dto.FileStructureAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.dto.FinalAnalysisDto;
import com.example.projectaianalyzer.domain.project.model.FileInfo;
import com.example.projectaianalyzer.infra.util.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisManagerImplTest {

    @Mock
    private DomainAnalysisService domainAnalysisService;

    @Mock
    private JsonParser jsonParser;

    @InjectMocks
    private AnalysisManagerImpl manager;

    @Test
    @DisplayName("Given valid structures When analyze Then return FinalAnalysisDto")
    void Given_ValidStructures_When_Analyze_Then_ReturnFinal() {
        List<FileStructureAnalysisDto> structureList = List.of(
                new FileStructureAnalysisDto("domainA", List.of("A.java"), "high", "reason"),
                new FileStructureAnalysisDto("domainB", List.of("B.java"), "medium", "reason")
        );

        // FileInfo constructor: (fileName, relativePath, absolutePath, extension, role, projectInfo, content, summary)
        // Make relativePath match the entries in structureList ("A.java", "B.java") so AnalysisManager can find them.
        FileInfo fileInfo1 = new FileInfo("A.java", "A.java", "/A.java", "java", "roleA", null, "c", null);
        FileInfo fileInfo2 = new FileInfo("B.java", "B.java", "/B.java", "java", "roleB", null, "c", null);
        List<FileInfo> fileInfos = List.of(fileInfo1, fileInfo2);


        when(domainAnalysisService.analyzeByRole(anyString(), any(), anyList()))
                .thenReturn("{result}");
        when(domainAnalysisService.analyzeDomainByPriority(anyString(), anyString(), anyList()))
                .thenReturn("{priority}");
        when(domainAnalysisService.analyzeEntireProjectByAllDomains(anyList()))
                .thenReturn("{final}");
        FinalAnalysisDto expectedDto = new FinalAnalysisDto();
        expectedDto.setProjectOverview("done");
        when(jsonParser.parseJson(eq("{final}"), any(TypeReference.class)))
                .thenReturn(expectedDto);

        FinalAnalysisDto result = manager.analyzeByFileStructureResult(
                structureList, fileInfos, "/tmp"
        );

        assertEquals("done", result.getProjectOverview());
        verify(domainAnalysisService, atLeastOnce()).analyzeByRole(anyString(), any(), anyList());
        verify(domainAnalysisService, times(1)).analyzeEntireProjectByAllDomains(anyList());
    }

    @Test
    @DisplayName("빈 파일 목록을 넣으면 analyzeByFileStructureResult에서 바로 IllegalArgumentException을 던진다.")
    void Given_MissingFiles_When_Analyze_Then_Throw() {
        List<FileStructureAnalysisDto> structureList = List.of(
                new FileStructureAnalysisDto("domainA", List.of(), "high", "reason")
        );

        assertThrows(IllegalArgumentException.class,
                () -> manager.analyzeByFileStructureResult(structureList, List.of(), "/tmp"));
    }
}