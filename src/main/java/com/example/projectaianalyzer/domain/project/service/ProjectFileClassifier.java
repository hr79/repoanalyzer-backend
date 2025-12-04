package com.example.projectaianalyzer.domain.project.service;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Set;

@Component
public class ProjectFileClassifier {
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
            "md", "txt", "doc", "docx", "pdf", "xls", "xlsx",
            "csv", "rtf", "odt", "ods", "odp", "odg", "odf",
            "tex", "pages", "key", "numbers", "ppt", "pptx",
            "epub", "mobi", "azw", "azw3", "djvu", "log",
            "msg", "eml", "oft", "sxw", "wpd", "wps"
    );

    public boolean isDocumentFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return DOCUMENT_EXTENSIONS.stream()
                .anyMatch(ext -> fileName.endsWith("." + ext));
    }

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "svg", "bmp", "webp",
            "tiff", "tif", "ico", "psd", "fig", "heic", "avif",
            "raw", "exr", "tga"
    );

    public boolean isImageFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return IMAGE_EXTENSIONS.stream()
                .anyMatch(ext -> fileName.endsWith("." + ext));
    }


    public String classifyExtension(Path filePath) {
        System.out.println("filePath: " + filePath);
        String fileName = filePath.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".java")) return "JAVA";
        if (fileName.endsWith(".kt")) return "KOTLIN";
        if (fileName.endsWith(".py")) return "PYTHON";
        if (fileName.endsWith(".js")) return "JAVASCRIPT";
        if (fileName.endsWith(".ts")) return "TYPESCRIPT";
        if (fileName.endsWith(".dart")) return "DART";
        if (fileName.endsWith(".cs")) return "CSHARP";
        if (fileName.endsWith(".c")) return "C";
        if (fileName.endsWith(".cpp")) return "CPP";
        if (fileName.endsWith(".h")) return "HEADER";
        if (fileName.endsWith(".php")) return "PHP";
        if (fileName.endsWith(".rb")) return "RUBY";
        if (fileName.endsWith(".swift")) return "SWIFT";
        if (fileName.endsWith(".html")) return "HTML";
        if (fileName.endsWith(".css")) return "CSS";
        if (fileName.endsWith(".json")) return "JSON";
        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) return "YAML";
        if (fileName.endsWith(".xml")) return "XML";
        if (fileName.endsWith(".properties")) return "PROPERTIES";
        if (fileName.endsWith(".env")) return "ENV";
        if (fileName.endsWith(".md")) return "MARKDOWN";

        return "OTHER";
    }

    public String classifyRole(Path filePath) {
        String pathString = filePath.toString().toLowerCase();
        String fileName = filePath.getFileName().toString().toLowerCase();

        // Backend MVC + extended architecture
        if (pathString.contains("/controller/") || fileName.contains("controller")) return "controller";
        if (pathString.contains("/service/") || fileName.contains("service")) return "service";
        if (pathString.contains("/repository/") || fileName.contains("repository") ||
            pathString.contains("/dao/") || fileName.contains("dao")) return "repository";
        if (pathString.contains("/dto/") || fileName.contains("dto")) return "dto";
        if (pathString.contains("/config/") || fileName.contains("config") || fileName.endsWith(".properties")) return "configuration";
        if (pathString.contains("/util/") || pathString.contains("/helper/") || fileName.contains("util") || fileName.contains("helper")) return "utility";
        if (pathString.contains("/filter/") || fileName.contains("filter")) return "filter";
        if (pathString.contains("/interceptor/") || fileName.contains("interceptor")) return "interceptor";
        if (pathString.contains("/exception/") || fileName.contains("exception")) return "exception";

        // Model / Entity / Domain-based detection
        if (pathString.contains("/entity/") || pathString.contains("/model/") || pathString.contains("/domain/")) {
            if (!fileName.contains("controller") && !fileName.contains("service") &&
                !fileName.contains("repository") && !fileName.contains("config") &&
                !fileName.contains("dto")) {
                return "model";
            }
        }

        // Test files
        if (pathString.contains("/test/") || fileName.contains("test")) return "test";

        // Frontend structure detection
        if (pathString.contains("/component/") || pathString.contains("/components/")) return "frontend-component";
        if (pathString.contains("/pages/") || pathString.contains("/views/")) return "frontend-view";
        if (pathString.contains("/store/") || pathString.contains("/state/")) return "frontend-state";
        if (pathString.contains("/hooks/")) return "frontend-hook";
        if (pathString.contains("/assets/") || pathString.contains("/static/")) return "frontend-asset";

        // Common/shared layer
        if (pathString.contains("/common/") || fileName.contains("common")) return "common";

        return "other";
    }
}
