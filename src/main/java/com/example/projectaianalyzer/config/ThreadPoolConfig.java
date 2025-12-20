package com.example.projectaianalyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolConfig {
    @Bean("analysisExecutor")
    public ExecutorService analysisExecutor() {
        int threads = 6;

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
}
