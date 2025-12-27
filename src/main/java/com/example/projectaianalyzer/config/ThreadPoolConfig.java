package com.example.projectaianalyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolConfig {
    @Bean("analysisExecutor")
    public ExecutorService analysisExecutor() {
        int threads = 2;

        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                threads,
                threads,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(30), // 큐 짧게 설정
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy() // 오버플로우 시 즉시 실패
        );

        return Executors.unconfigurableExecutorService(poolExecutor);
    }

    @Bean("groqExecutor")
    public ExecutorService groqExecutor() {
        // ✅ 수정: 스레드 풀 크기 증가 (3 -> 6)
        // 동시에 여러 Groq API 요청을 처리하고 429 에러 발생 시 일부 스레드가 블로킹되어도
        // 다른 요청을 처리할 수 있는 여유 확보
        int threads = 4;
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                threads,
                threads,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(50),  // 큐 크기도 증가 (30 -> 50)
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
        return Executors.unconfigurableExecutorService(poolExecutor);
    }
}
