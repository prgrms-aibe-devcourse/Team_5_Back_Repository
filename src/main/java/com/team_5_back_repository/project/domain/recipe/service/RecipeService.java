package com.team_5_back_repository.project.domain.recipe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.recipe.dto.*;
import com.team_5_back_repository.project.domain.recipe.entity.Recipe;
import com.team_5_back_repository.project.domain.recipe.enums.CookingTime;
import com.team_5_back_repository.project.domain.recipe.enums.Difficulty;
import com.team_5_back_repository.project.domain.recipe.enums.RecipeCategory;
import com.team_5_back_repository.project.domain.recipe.enums.RecipeStatus;
import com.team_5_back_repository.project.domain.recipe.infra.OpenAiClient;
import com.team_5_back_repository.project.domain.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private static final Logger log = LoggerFactory.getLogger(RecipeService.class);

    private final RecipeRepository recipeRepository;
    private final OpenAiClient openAiClient;
    private final YoutubeService youtubeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 비회원/회원 AI 레시피 생성
    public List<RecipeResponse> generate(Member member, RecipeGenerateRequest req) throws Exception {
        // 시스템 프롬프트
        String systemPrompt = """
                너는 한국어로 레시피를 만드는 AI 셰프야.
                반드시 아래 JSON 구조를 지켜서 응답해야 하며,
                userPrompt에서 요청한 count 개수만큼" recipes 배열에 넣어야 해.
                절대 1개만 생성하지 말고, 요청된 count 수만큼 정확히 생성해라.
                "cookingTime"의 값으로는 UNDER_10, FROM_10_TO_20, FROM_20_TO_30, OVER_30 중 하나만 가능해.
                
                JSON 스키마:
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
                        """,
                req.getPrompt(),
                req.getCategory(),
                req.getCookingTime(),
                req.getDifficulty(),
                req.getServings(),
                req.getCount()
        );

        // OpenAI API 호출
        String content = openAiClient.callOpenAi(systemPrompt, userPrompt);

        JsonNode root = objectMapper.readTree(content); // JSON 문자열을 JsonNode 트리 구조로 파싱
        JsonNode items = root.path("recipes"); // root.recipes 배열 접근

        List<RecipeResponse> result = new ArrayList<>();

        // 모든 레시피를 하나씩 DTO 형태로 변환
        for (JsonNode node : items) {
            // ingredients: JSON 배열 → List<String>
            List<String> ingredients = objectMapper.convertValue(
                    node.path("ingredients"), new TypeReference<>() {
                    }
            );

            // steps: JSON 배열 → List<String>
            List<String> steps = objectMapper.convertValue(
                    node.path("steps"), new TypeReference<>() {
                    }
            );

            String title = node.path("title").asText();

            String youtubeUrl = null;
            try {
                YoutubeVideoResponse video = youtubeService.searchByTitle(title);
                youtubeUrl = video.getEmbedUrl();
            } catch (Exception e) {
                log.warn("Failed to fetch YouTube video for title {}: {}", title, e.getMessage());
            }

            Recipe recipe = Recipe.builder()
                    .member(member)
                    .title(title)
                    .description(node.path("description").asText())
                    .category(RecipeCategory.valueOf(node.path("category").asText()))
                    .cookingTime(CookingTime.valueOf(node.path("cookingTime").asText()))
                    .difficulty(Difficulty.valueOf(node.path("difficulty").asText()))
                    .servings(node.path("servings").asInt())
                    .ingredients(objectMapper.writeValueAsString(ingredients))
                    .steps(objectMapper.writeValueAsString(steps))
                    .youtubeUrl(youtubeUrl)
                    .status(RecipeStatus.GENERATED)
                    .build();

            // 회원일 경우에만 DB 저장됨
            if (member != null) {
                recipeRepository.save(recipe);
            }

            result.add(RecipeResponse.builder()
                    .id(recipe.getId())
                    .title(recipe.getTitle())
                    .description(recipe.getDescription())
                    .category(recipe.getCategory())
                    .cookingTime(recipe.getCookingTime())
                    .difficulty(recipe.getDifficulty())
                    .servings(recipe.getServings())
                    .ingredients(ingredients)
                    .steps(steps)
                    .status(recipe.getStatus())
                    .youtubeUrl(recipe.getYoutubeUrl())
                    .build());
        }
        return result;
    }

    // 회원이 AI 레시피 저장
    public Long save(Member member, RecipeSaveRequest req) throws Exception {
        Recipe recipe = Recipe.builder()
                .member(member)
                .title(req.getTitle())
                .description(req.getDescription())
                .category(req.getCategory())
                .cookingTime(req.getCookingTime())
                .difficulty(req.getDifficulty())
                .servings(req.getServings())
                .ingredients(objectMapper.writeValueAsString(req.getIngredients()))
                .steps(objectMapper.writeValueAsString(req.getSteps()))
                .youtubeUrl(req.getYoutubeUrl())
                .status(RecipeStatus.SAVED)
                .build();

        recipeRepository.save(recipe);
        return recipe.getId();
    }

    // (사이드바) 회원이 생성한 모든 레시피
    public List<RecipeResponse> getGeneratedRecipes(Long memberId) throws Exception {
        List<Recipe> list = recipeRepository.findAllByMemberIdAndStatusOrderByIdDesc(memberId, RecipeStatus.GENERATED);

        return convert(list);
    }

    // (마이페이지) 회원이 저장한 레시피만
    public List<RecipeResponse> getSavedRecipes(Long memberId) throws Exception {
        List<Recipe> list = recipeRepository.findAllByMemberIdAndStatus(memberId, RecipeStatus.SAVED);

        return convert(list);
    }

    // 레시피 삭제
    public void delete(Long recipeId, Long memberId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피 없음"));

        if (!recipe.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인 레시피만 삭제할 수 있습니다.");
        }

        recipeRepository.delete(recipe);
    }

    // 공유 링크 생성
    public ShareLinkResponse createShareLink(Long recipeId, Long memberId, String frontendBaseUrl) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피 없음"));

        // 본인 레시피인지 확인
        if (recipe.getMember() == null || !recipe.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인 레시피만 공유할 수 있습니다.");
        }

        // 이미 공유 토큰이 있으면 재사용, 없으면 새로 생성
        String shareToken = recipe.getShareToken();
        if (shareToken == null || shareToken.isEmpty()) {
            shareToken = UUID.randomUUID().toString().replace("-", "");
            recipe.setShareToken(shareToken);
            recipeRepository.save(recipe);
        }

        String shareUrl = frontendBaseUrl + "/recipe/shared/" + shareToken;

        return ShareLinkResponse.builder()
                .shareToken(shareToken)
                .shareUrl(shareUrl)
                .build();
    }

    // 공유 링크로 레시피 조회
    public RecipeResponse getRecipeByShareToken(String shareToken) throws Exception {
        Recipe recipe = recipeRepository.findByShareToken(shareToken);
        if (recipe == null) {
            throw new IllegalArgumentException("공유 링크가 유효하지 않습니다.");
        }

        List<String> ingredients = objectMapper.readValue(
                recipe.getIngredients(), new TypeReference<>() {}
        );

        List<String> steps = objectMapper.readValue(
                recipe.getSteps(), new TypeReference<>() {}
        );

        return RecipeResponse.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .category(recipe.getCategory())
                .cookingTime(recipe.getCookingTime())
                .difficulty(recipe.getDifficulty())
                .servings(recipe.getServings())
                .ingredients(ingredients)
                .steps(steps)
                .status(recipe.getStatus())
                .youtubeUrl(recipe.getYoutubeUrl())
                .build();
    }

    // 공통 변환 메서드
    private List<RecipeResponse> convert(List<Recipe> list) throws Exception {
        List<RecipeResponse> result = new ArrayList<>();

        for (Recipe r : list) {
            List<String> ingredients = objectMapper.readValue(
                    r.getIngredients(), new TypeReference<>() {}
            );

            List<String> steps = objectMapper.readValue(
                    r.getSteps(), new TypeReference<>() {}
            );

            result.add(RecipeResponse.builder()
                    .id(r.getId())
                    .title(r.getTitle())
                    .description(r.getDescription())
                    .category(r.getCategory())
                    .cookingTime(r.getCookingTime())
                    .difficulty(r.getDifficulty())
                    .servings(r.getServings())
                    .ingredients(ingredients)
                    .steps(steps)
                    .status(r.getStatus())
                    .youtubeUrl(r.getYoutubeUrl())
                    .build());
        }
        return result;
    }
}