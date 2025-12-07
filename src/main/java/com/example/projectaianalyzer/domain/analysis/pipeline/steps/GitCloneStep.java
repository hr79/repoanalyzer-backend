package com.example.projectaianalyzer.domain.analysis.pipeline.steps;

import com.example.projectaianalyzer.domain.project.service.GitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitCloneStep implements AnalysisStep<String, String> {

    private final GitService gitService;

    public static final String BASE_PATH = "tmp/repo/";

    @Override
    public String execute(String repoUrl) {
        String savePath = repoUrl
                .replace("https://github.com/", "")
                .replace(".git", "");
        savePath = BASE_PATH + savePath;
        log.info("savePath: {}", savePath);

        gitService.cloneRepository(repoUrl, savePath);

        return savePath;
    }
}
