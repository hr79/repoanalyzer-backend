package com.example.projectaianalyzer.domain.project.service;

public interface GitService {
    void cloneRepository(String repoUrl, String filePath);
}
