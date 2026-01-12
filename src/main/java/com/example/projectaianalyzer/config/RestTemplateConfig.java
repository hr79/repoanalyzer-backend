package com.example.projectaianalyzer.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    // ✅ RestTemplate 동시성 및 타임아웃 설정
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // ✅ 타임아웃 설정
        factory.setConnectTimeout(10000);  // 10초 (연결)
        factory.setReadTimeout(60000);  // 60초 (응답 대기) - Groq API는 응답이 오래 걸릴 수 있음

        // ✅ 버퍼링 활성화 (응답 재읽기 가능)
        BufferingClientHttpRequestFactory bufferingFactory = new BufferingClientHttpRequestFactory(factory);

        return new RestTemplate(bufferingFactory);
    }
}
