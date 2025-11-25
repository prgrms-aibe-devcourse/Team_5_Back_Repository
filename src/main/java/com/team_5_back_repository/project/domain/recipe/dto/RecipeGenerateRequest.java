package com.team_5_back_repository.project.domain.recipe.dto;

import com.team_5_back_repository.project.domain.recipe.enums.CookingTime;
import com.team_5_back_repository.project.domain.recipe.enums.Difficulty;
import com.team_5_back_repository.project.domain.recipe.enums.RecipeCategory;
import lombok.Getter;

@Getter
public class RecipeGenerateRequest {
    private String prompt; // 사용자 입력 설명
    private RecipeCategory category;
    private CookingTime cookingTime;
    private Difficulty difficulty;
    private int servings;
    private int count; // 기본 3개 생성
}
