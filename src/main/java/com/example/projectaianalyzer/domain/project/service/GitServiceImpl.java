package com.example.projectaianalyzer.domain.project.service;

import com.example.projectaianalyzer.infra.util.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitServiceImpl implements GitService {

    private final FileStorage fileStorage;

    private static final int MAX_RETRY_COUNT = 3;

    @Override
    public void cloneRepository(String repoUrl, String filePath) {
        int attempt = 0;
        Path absolutePath = Paths.get(filePath).toAbsolutePath();
        log.info("absolutePath: {}", absolutePath);
        while (attempt < MAX_RETRY_COUNT){
            try {
                attempt++;
                log.info(":::: save project file from git :::");
                Git.cloneRepository()
                        .setURI(repoUrl)
                        .setDirectory(new File(absolutePath.toString()))
                        .call();
            } catch (GitAPIException e) {
                log.error("git clone failed: {}", e.getMessage(), e);
                throw new RuntimeException("git clone failed", e);
            } catch (JGitInternalException e) {
                log.error("git clone failed: {}", e.getMessage(), e);

                if (attempt >= MAX_RETRY_COUNT) {
                    log.error("최대 재시도 횟수에 도달했습니다. 클론 작업을 중단합니다.");
                    throw e;
                }

                log.info("기존에 클론된 디렉토리가 있어 발생한 문제일 수 있습니다. 기존 디렉토리를 삭제하고 다시 시도합니다.");
                log.info("filePath: {}", filePath);
                fileStorage.delete(filePath);
            }
        }
    }
}
