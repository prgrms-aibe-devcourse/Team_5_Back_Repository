package com.team_5_back_repository.project.domain.recipe.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .connectTimeout(Duration.ofSeconds(5)) // 연결 타임아웃을 5초로 설정
                .readTimeout(Duration.ofSeconds(10)) // 읽기 타임아웃을 5초로 설정
                .build();
    }
}