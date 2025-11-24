package com.example.projectaianalyzer.domain.project.service;

import com.example.projectaianalyzer.domain.project.model.FileInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProjectFileScannerImplTest {

    @Mock
    private ProjectFileClassifier projectFileClassifier;

    @Mock
    private ProjectTypeDetector projectTypeDetector;

    @InjectMocks
    private ProjectFileScannerImpl scanner;

    @Test
    @DisplayName("Given valid directory When scan Then return FileInfo list")
    void Given_ValidDir_When_Scan_Then_ReturnFileInfos() {
        List<FileInfo> result = assertDoesNotThrow(() ->
                scanner.scanProjectDirectory("/tmp", List.of(), List.of())
        );
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given IO failure When scan Then throw RuntimeException")
    void Given_IOFail_When_Scan_Then_Throw() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> scanner.scanProjectDirectory("/not/exist", List.of(), List.of()));
        assertTrue(ex.getMessage().contains("스캔할 프로젝트 경로가 유효하지 않습니다."));
    }
}