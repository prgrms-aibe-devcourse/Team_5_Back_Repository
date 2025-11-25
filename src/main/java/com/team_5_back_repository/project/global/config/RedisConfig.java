package com.team_5_back_repository.project.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * Redis 연결을 위한 ConnectionFactory 빈 생성
     * Lettuce: Redis 클라이언트 라이브러리 (비동기, 리액티브 지원)
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * Redis 데이터 저장/조회를 위한 RedisTemplate 빈 생성
     * - Key: String 타입으로 직렬화
     * - Value: JSON 타입으로 직렬화 (LocalDateTime 지원)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // ObjectMapper 설정 (LocalDateTime 직렬화 지원)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // Java 8 날짜/시간 API 지원
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // ISO-8601 형식 사용

        // JSON 직렬화 설정
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Key Serializer: String
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 기본 Serializer 설정
        template.setDefaultSerializer(serializer);

        // Key-Value Serializer 설정
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);

        // Hash Key-Value Serializer 설정
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(serializer);

        // 설정 완료 후 초기화
        template.afterPropertiesSet();

        return template;
    }
}