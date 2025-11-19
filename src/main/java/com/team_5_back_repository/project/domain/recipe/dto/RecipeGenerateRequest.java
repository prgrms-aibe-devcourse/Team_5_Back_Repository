package com.team_5_back_repository.project.domain.recipe.dto;

import com.team_5_back_repository.project.domain.recipe.enums.CookingTime;
import com.team_5_back_repository.project.domain.recipe.enums.Difficulty;
import com.team_5_back_repository.project.domain.recipe.enums.RecipeCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeGenerateRequest {
    private String prompt;             // 사용자 입력 설명
    private RecipeCategory category;   // 요리 카테고리
    private CookingTime cookingTime;   // 조리 시간
    private Difficulty difficulty;     // 난이도
    private int servings;              // 인분 수
    private int count = 3;             // 한번에 추천받을 레시피 개수 (기본 3)
}
