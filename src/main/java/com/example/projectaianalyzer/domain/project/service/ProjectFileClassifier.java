package com.example.projectaianalyzer.domain.project.service;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Set;
import java.util.Map;

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

    private static final Map<String, String> EXTENSION_TYPES = Map.ofEntries(
            Map.entry("java", "JAVA"),
            Map.entry("kt", "KOTLIN"),
            Map.entry("py", "PYTHON"),
            Map.entry("js", "JAVASCRIPT"),
            Map.entry("ts", "TYPESCRIPT"),
            Map.entry("dart", "DART"),
            Map.entry("cs", "CSHARP"),
            Map.entry("c", "C"),
            Map.entry("cpp", "CPP"),
            Map.entry("h", "HEADER"),
            Map.entry("php", "PHP"),
            Map.entry("rb", "RUBY"),
            Map.entry("swift", "SWIFT"),
            Map.entry("html", "HTML"),
            Map.entry("css", "CSS"),
            Map.entry("json", "JSON"),
            Map.entry("yaml", "YAML"),
            Map.entry("yml", "YAML"),
            Map.entry("xml", "XML"),
            Map.entry("properties", "PROPERTIES"),
            Map.entry("env", "ENV"),
            Map.entry("md", "MARKDOWN")
    );

    public String classifyExtension(Path filePath) {
        System.out.println("filePath: " + filePath);
        String fileName = filePath.getFileName().toString().toLowerCase();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "OTHER";
        }
        String ext = fileName.substring(dotIndex + 1);
        return EXTENSION_TYPES.getOrDefault(ext, "OTHER");
    }

    public String classifyRole(Path filePath) {
        String path = filePath.toString().toLowerCase();
        String file = filePath.getFileName().toString().toLowerCase();

        // Ordered matching rules
        record Rule(String keyword, String role) {}
        Rule[] rules = new Rule[] {
                new Rule("/controller", "controller"),
                new Rule("controller", "controller"),

                new Rule("/service", "service"),
                new Rule("service", "service"),

                new Rule("/repository", "repository"),
                new Rule("repository", "repository"),
                new Rule("/dao", "repository"),
                new Rule("dao", "repository"),

                new Rule("/dto", "dto"),
                new Rule("dto", "dto"),

                new Rule("/config", "configuration"),
                new Rule("config", "configuration"),
                new Rule(".properties", "configuration"),

                new Rule("/util", "utility"),
                new Rule("/helper", "utility"),
                new Rule("util", "utility"),
                new Rule("helper", "utility"),

                new Rule("/filter", "filter"),
                new Rule("filter", "filter"),

                new Rule("/interceptor", "interceptor"),
                new Rule("interceptor", "interceptor"),

                new Rule("/exception", "exception"),
                new Rule("exception", "exception"),

                new Rule("/test", "test"),
                new Rule("test", "test"),

                new Rule("/component", "frontend-component"),
                new Rule("/components", "frontend-component"),

                new Rule("/pages", "frontend-view"),
                new Rule("/views", "frontend-view"),

                new Rule("/store", "frontend-state"),
                new Rule("/state", "frontend-state"),

                new Rule("/hooks", "frontend-hook"),

                new Rule("/assets", "frontend-asset"),
                new Rule("/static", "frontend-asset"),

                new Rule("/common", "common"),
                new Rule("common", "common")
        };

        for (Rule rule : rules) {
            if (path.contains(rule.keyword()) || file.contains(rule.keyword())) {
                return rule.role();
            }
        }

        // Model detection (special case)
        if (path.contains("/entity") || path.contains("/model") || path.contains("/domain")) {
            if (!file.contains("controller")
                    && !file.contains("service")
                    && !file.contains("repository")
                    && !file.contains("config")
                    && !file.contains("dto")) {
                return "model";
            }
        }

        return "other";
    }
}
