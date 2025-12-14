package com.team_5_back_repository.project.domain.recipe.dto;

import com.team_5_back_repository.project.domain.recipe.enums.CookingTime;
import com.team_5_back_repository.project.domain.recipe.enums.Difficulty;
import com.team_5_back_repository.project.domain.recipe.enums.RecipeCategory;
import lombok.Getter;

import java.util.List;

@Getter
public class RecipeSaveRequest {
    private String title;
    private String description;

    private RecipeCategory category;
    private CookingTime cookingTime;
    private Difficulty difficulty;
    private int servings;

    private List<String> ingredients;
    private List<String> steps;

    private String youtubeUrl;
}
