package com.example.projectaianalyzer.domain.project.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class GitServiceImpl implements GitService {
    @Override
    public void cloneRepository(String repoUrl, String filePath) {
        try {
            log.info(":::: save project file from git :::");
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(filePath))
                    .call();
        } catch (GitAPIException e) {
            log.error("git clone failed: {}", e.getMessage(), e);
            throw new RuntimeException("git에서 프로젝트를 가져올 수 없습니다.", e);
        }
    }
}
