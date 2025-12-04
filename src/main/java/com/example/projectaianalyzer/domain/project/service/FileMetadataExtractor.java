package com.example.projectaianalyzer.domain.project.service;

import com.example.projectaianalyzer.domain.project.model.FileInfo;
import com.example.projectaianalyzer.domain.project.model.ProjectInfo;
import com.example.projectaianalyzer.infra.util.FileContentLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class FileMetadataExtractor {
    private final ProjectFileClassifier projectFileClassifier;

    public FileInfo extract(Path path, Path rootPath, ProjectInfo projectInfo) {
        if (projectFileClassifier.isDocumentFile(path)
                || projectFileClassifier.isImageFile(path)) {
            return null;
        }

        String extension = projectFileClassifier.classifyExtension(path);
        String role = projectFileClassifier.classifyRole(path);

        String rawCode = FileContentLoader.loadFileContent(path);
        String content = getContent(rawCode);

        return FileInfo.builder()
                .fileName(path.getFileName().toString())
                .relativePath(rootPath.relativize(path).toString())
                .absolutePath(path.toAbsolutePath().toString())
                .extension(extension)
                .role(role)
                .content(content)
                .projectInfo(projectInfo)
                .build();
    }

    private String getContent(String rawCode) {
        if (rawCode == null) return "";
        return rawCode
                .replaceAll("package .*?;", "")
                .replaceAll("import .*?;", "");
    }
}
