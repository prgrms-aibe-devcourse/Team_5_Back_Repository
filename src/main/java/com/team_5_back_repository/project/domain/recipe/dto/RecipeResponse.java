package com.team_5_back_repository.project.domain.recipe.dto;

import com.team_5_back_repository.project.domain.recipe.enums.CookingTime;
import com.team_5_back_repository.project.domain.recipe.enums.Difficulty;
import com.team_5_back_repository.project.domain.recipe.enums.RecipeCategory;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponse {

    private Long id;                   // DB 저장 레시피이면 id 존재
    private String title;
    private String description;

    private RecipeCategory category;
    private CookingTime cookingTime;
    private Difficulty difficulty;
    private int servings;

    private List<String> ingredients;  // 재료 목록
    private List<String> steps;        // 조리 순서 목록
}
