package com.team_5_back_repository.project.domain.recipe.infra;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// OpenAI ChatCompletion API를 호출하는 클래스
@Service
@RequiredArgsConstructor
public class OpenAiClient {
    private final RestTemplate restTemplate;
    private final String url = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    public String callOpenAi(String systemPrompt, String userPrompt) {

        // 바디 정의
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", new Object[]{
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                },
                "temperature", 0.7
        );

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 바디 + 헤더 합치기
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, headers);

        // exchange()라는 http 요청 메서드 호출 (URL, 메서드 방식, 요청 엔티티, 응답 타입)
        ResponseEntity<JsonNode> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        httpEntity,
                        JsonNode.class
                );

        // message.content만 반환
        return response.getBody()
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
    }
}
