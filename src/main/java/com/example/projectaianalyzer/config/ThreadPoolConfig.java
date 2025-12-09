package com.example.projectaianalyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolConfig {
    @Bean("analysisExecutor")
    public ExecutorService analysisExecutor(){
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors());
        int poolSize = cores; // 스레드 수를 cpu 코어 수로 설정, 필요 시 core * 2 같이 조정
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy() // 오버플로우시 호출 스레드에서 실행
        );
        poolExecutor.allowCoreThreadTimeOut(true);

        return Executors.unconfigurableExecutorService(poolExecutor);
    }
}
