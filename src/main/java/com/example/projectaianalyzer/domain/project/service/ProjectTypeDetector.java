package com.example.projectaianalyzer.domain.project.service;

import com.example.projectaianalyzer.domain.project.model.ProjectFramework;
import com.example.projectaianalyzer.domain.project.model.ProjectInfo;
import com.example.projectaianalyzer.domain.project.model.ProjectLanguage;
import com.example.projectaianalyzer.domain.project.model.ProjectType;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ProjectTypeDetector {

    public ProjectInfo detectFramework(Path path) {
        System.out.println("::::프로젝트 타입 감지 시작::::");
        System.out.println("::::path = " + path);

        String fileName = path.getFileName().toString();
        String parentDir = path.getParent().toString();

        // Java Spring Boot (pom.xml or Gradle)
        if (fileName.equals("pom.xml") || fileName.equals("build.gradle") || fileName.equals("build.gradle.kts")) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.BACKEND.toString())
                    .projectLanguage(ProjectLanguage.JAVA.toString())
                    .projectFramework(ProjectFramework.SPRING_BOOT.toString())
                    .basePath(parentDir)
                    .build();
        }

        // Node.js Express
        if (fileName.equals("package.json") && containsKeywordInFile(path, "\"express\"")) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.BACKEND.toString())
                    .projectLanguage(ProjectLanguage.JAVASCRIPT.toString())
                    .projectFramework(ProjectFramework.NODE_EXPRESS.toString())
                    .basePath(parentDir)
                    .build();
        }

        // React
        if (fileName.equals("package.json") && (containsKeywordInFile(path, "\"react\"") || containsKeywordInFile(path, "\"vite\""))) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.FRONTEND.toString())
                    .projectLanguage(ProjectLanguage.JAVASCRIPT.toString())
                    .projectFramework(ProjectFramework.REACT.toString())
                    .basePath(parentDir)
                    .build();
        }

        // Vue.js
        if (fileName.equals("package.json") && containsKeywordInFile(path, "\"vue\"")) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.FRONTEND.toString())
                    .projectLanguage(ProjectLanguage.JAVASCRIPT.toString())
                    .projectFramework(ProjectFramework.VUE.toString())
                    .basePath(parentDir)
                    .build();
        }

        // Angular
        if (fileName.equals("angular.json") || (fileName.equals("package.json") && containsKeywordInFile(path, "\"@angular/core\""))) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.FRONTEND.toString())
                    .projectLanguage(ProjectLanguage.TYPESCRIPT.toString())
                    .projectFramework(ProjectFramework.ANGULAR.toString())
                    .basePath(parentDir)
                    .build();
        }

        // Next.js
        if (fileName.equals("package.json") && containsKeywordInFile(path, "\"next\"")) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.FRONTEND.toString())
                    .projectLanguage(ProjectLanguage.JAVASCRIPT.toString())
                    .projectFramework(ProjectFramework.NEXT_JS.toString())
                    .basePath(parentDir)
                    .build();
        }

        // Nuxt.js
        if (fileName.equals("package.json") && containsKeywordInFile(path, "\"nuxt\"")) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.FRONTEND.toString())
                    .projectLanguage(ProjectLanguage.JAVASCRIPT.toString())
                    .projectFramework(ProjectFramework.NUXTJS.toString())
                    .basePath(parentDir)
                    .build();
        }

        // Svelte
        if (fileName.equals("package.json") && containsKeywordInFile(path, "\"svelte\"")) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.FRONTEND.toString())
                    .projectLanguage(ProjectLanguage.JAVASCRIPT.toString())
                    .projectFramework(ProjectFramework.SVELTE.toString())
                    .basePath(parentDir)
                    .build();
        }

        // Flutter
        if (fileName.equals("pubspec.yaml")) {
            if (containsDirectory(parentDir, "lib")) {
                return ProjectInfo.builder()
                        .projectType(ProjectType.FRONTEND.toString())
                        .projectLanguage(ProjectLanguage.DART.toString())
                        .projectFramework(ProjectFramework.FLUTTER.toString())
                        .basePath(parentDir)
                        .build();
            } else if (containsDirectory(parentDir, "bin")) {
                return ProjectInfo.builder()
                        .projectType(ProjectType.BACKEND.toString())
                        .projectLanguage(ProjectLanguage.DART.toString())
                        .basePath(parentDir)
                        .build();
            }
        }

        // Django
        if (fileName.equals("manage.py") || containsDirectory(parentDir, "django_project")) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.BACKEND.toString())
                    .projectLanguage(ProjectLanguage.PYTHON.toString())
                    .projectFramework(ProjectFramework.DJANGO.toString())
                    .basePath(parentDir)
                    .build();
        }

        // Flask
        if (fileName.equals("app.py") && containsKeywordInFile(path, "from flask import")) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.BACKEND.toString())
                    .projectLanguage(ProjectLanguage.PYTHON.toString())
                    .projectFramework(ProjectFramework.FLASK.toString())
                    .basePath(parentDir)
                    .build();
        }

        // Laravel
        if (fileName.equals("artisan") || containsDirectory(parentDir, "app/Http")) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.BACKEND.toString())
                    .projectLanguage(ProjectLanguage.PHP.toString())
                    .projectFramework(ProjectFramework.LARAVEL.toString())
                    .basePath(parentDir)
                    .build();
        }

        // Ruby on Rails
        if (fileName.equals("Gemfile") && containsKeywordInFile(path, "rails")) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.BACKEND.toString())
                    .projectLanguage(ProjectLanguage.RUBY.toString())
                    .projectFramework(ProjectFramework.RAILS.toString())
                    .basePath(parentDir)
                    .build();
        }

        // ASP.NET Core
        if (fileName.endsWith(".csproj") || fileName.endsWith(".sln")) {
            return ProjectInfo.builder()
                    .projectType(ProjectType.BACKEND.toString())
                    .projectLanguage(ProjectLanguage.CSHARP.toString())
                    .projectFramework(ProjectFramework.ASP_DOTNET.toString())
                    .basePath(parentDir)
                    .build();
        }

        return null;
    }

    private boolean containsDirectory(String parentDir, String dirName) {
        return Files.exists(Paths.get(parentDir, dirName));
    }

    private boolean containsKeywordInFile(Path path, String keyword) {
        try {
            String content = Files.readString(path);
            return content.contains(keyword);
        } catch (Exception e) {
            return false;
        }
    }
}
