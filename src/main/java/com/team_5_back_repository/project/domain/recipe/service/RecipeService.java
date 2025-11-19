package com.team_5_back_repository.project.domain.recipe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.recipe.dto.RecipeGenerateRequest;
import com.team_5_back_repository.project.domain.recipe.dto.RecipeResponse;
import com.team_5_back_repository.project.domain.recipe.dto.RecipeSaveRequest;
import com.team_5_back_repository.project.domain.recipe.entity.Recipe;
import com.team_5_back_repository.project.domain.recipe.enums.CookingTime;
import com.team_5_back_repository.project.domain.recipe.enums.Difficulty;
import com.team_5_back_repository.project.domain.recipe.enums.RecipeCategory;
import com.team_5_back_repository.project.domain.recipe.infra.OpenAiClient;
import com.team_5_back_repository.project.domain.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // AI 레시피 생성
    public List<RecipeResponse> generateRecipes(RecipeGenerateRequest req) throws Exception{
        // OpenAI에 전달할 시스템 프롬프트
        String systemPrompt = """
                너는 한국어로 레시피를 만드는 AI 셰프야.
                아래 JSON 구조를 반드시 지켜서만 응답해.

                {
                  "recipes": [
                    {
                      "title": "",
                      "description": "",
                      "category": "",
                      "cookingTime": "",
                      "difficulty": "",
                      "servings": 1,
                      "ingredients": [],
                      "steps": []
                    }
                  ]
                }
                """;

        // 사용자 프롬프트
        String userPrompt = String.format("""
                요리 설명: %s
                카테고리: %s
                조리시간: %s
                난이도: %s
                인분: %d
                개수: %d

                1인 가구 기준으로 간단하고 저렴한 재료로 작성해줘.
                """,
                req.getPrompt(),
                req.getCategory(),
                req.getCookingTime(),
                req.getDifficulty(),
                req.getServings(),
                req.getCount()
        );

        // OpenAI 호출
        String content = openAiClient.callOpenAi(systemPrompt, userPrompt);
        // JSON 문자열을 JsonNode 트리 구조로 파싱
        JsonNode root = objectMapper.readTree(content);
        // root.recipes 배열 접근
        JsonNode items = root.path("recipes");

        List<RecipeResponse> list = new ArrayList<>();
        // 모든 레시피를 하나씩 DTO 형태로 변환
        for (JsonNode node : items) {
            // ingredients: JSON 배열 → List<String>
            List<String> ingredients = objectMapper.convertValue(
                    node.path("ingredients"), new TypeReference<>(){}
            );

            // steps: JSON 배열 → List<String>
            List<String> steps = objectMapper.convertValue(
                    node.path("steps"), new TypeReference<>(){}
            );

            // DTO 생성
            RecipeResponse dto = RecipeResponse.builder()
                    .title(node.path("title").asText())
                    .description(node.path("description").asText())
                    .category(RecipeCategory.valueOf(node.path("category").asText()))
                    .cookingTime(CookingTime.valueOf(node.path("cookingTime").asText()))
                    .difficulty(Difficulty.valueOf(node.path("difficulty").asText()))
                    .servings(node.path("servings").asInt())
                    .ingredients(ingredients)
                    .steps(steps)
                    .build();

            list.add(dto);
        }
        return list;
    }

}
