package com.example.projectaianalyzer.domain.project.service;

import com.example.projectaianalyzer.domain.project.model.FileInfo;
import com.example.projectaianalyzer.domain.project.model.ProjectInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectFileScannerImplTest {

    @Mock
    private ProjectFileClassifier projectFileClassifier;

    @Mock
    private ProjectTypeDetector projectTypeDetector;

    @InjectMocks
    private ProjectFileScannerImpl scanner;

    @Test
    @DisplayName("유효한 임시 디렉토리 스캔 시 파일 정보 목록을 반환한다")
    void Given_TempDirWithFiles_When_Scan_Then_ReturnFileInfos(@TempDir Path tempDir) throws Exception {
        Path src = Files.createDirectories(tempDir.resolve("src/main/java"));
        Path fileA = Files.writeString(src.resolve("A.java"), "package a; class A {}");
        Path fileB = Files.writeString(src.resolve("B.java"), "package b; class B {}");

        when(projectFileClassifier.classifyExtension(any())).thenReturn("java");
        when(projectFileClassifier.classifyRole(any())).thenReturn("service");

        when(projectTypeDetector.detectFramework(any())).thenReturn(null);

        List<FileInfo> result = scanner.scanProjectDirectory(
                tempDir.toString(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        assertNotNull(result);
        assertEquals(2, result.size(), "임시 디렉토리에 생성한 파일 수와 일치해야 한다.");

        verify(projectFileClassifier, atLeast(1)).classifyExtension(any());
        verify(projectFileClassifier, atLeast(1)).classifyRole(any());
    }

    @Test
    @DisplayName("존재하지 않는 경로 스캔 시 IllegalArgumentException을 던져야 한다")
    void Given_InvalidPath_When_Scan_Then_ThrowException() {
        String invalidPath = "/this/path/does/not/exist_" + System.nanoTime();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> scanner.scanProjectDirectory(invalidPath, new ArrayList<>(), new ArrayList<>())
        );

        assertTrue(ex.getMessage().contains("스캔할 프로젝트 경로가 유효하지 않습니다."));
    }

    @Test
    @DisplayName("하위 디렉토리 스캔 중 IOException 발생 시 RuntimeException을 던져야 한다")
    void Given_IOErrorInDetectProjectType_When_Scan_Then_ThrowRuntimeException(@TempDir Path tempDir) throws Exception {
        Path fake = tempDir.resolve("fake.java");
        Files.writeString(fake, "class Fake{}");

        when(projectTypeDetector.detectFramework(any())).thenThrow(new RuntimeException("IO error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> scanner.scanProjectDirectory(tempDir.toString(), new ArrayList<>(), new ArrayList<>())
        );
        assertEquals("IO error", ex.getMessage());
    }
}