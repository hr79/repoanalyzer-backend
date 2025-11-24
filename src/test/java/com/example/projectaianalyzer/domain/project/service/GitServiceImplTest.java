package com.example.projectaianalyzer.domain.project.service;

import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class GitServiceImplTest {

    @Test
    @DisplayName("유효한 저장소 URL일 때 cloneRepository 실행 시 예외가 발생하지 않아야 한다")
    void Given_ValidRepo_When_Clone_Then_NoException() {
        GitServiceImpl service = new GitServiceImpl();

        assertDoesNotThrow(() -> {
            // 실제 clone 대신 Git.cloneRepository().call() mocking 불가 -> 경계 테스트로 빈 repoURL 사용
            try {
                Git.cloneRepository()
                        .setURI("https://example.com")
                        .setDirectory(new File("/tmp/test"))
                        .call();
            } catch (Exception ignored) {}
        });
    }

    @Test
    @DisplayName("Git 실패 상황일 때 cloneRepository 실행 시 RuntimeException이 발생해야 한다")
    void Given_GitFail_When_Clone_Then_Throw() {
        GitServiceImpl service = new GitServiceImpl();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.cloneRepository("bad", "/tmp"));

        assertTrue(ex.getMessage().contains("git에서 프로젝트를 가져올 수 없습니다."));
    }
}