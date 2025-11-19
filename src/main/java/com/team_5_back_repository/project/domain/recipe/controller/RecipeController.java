package com.team_5_back_repository.project.domain.recipe.controller;

import com.team_5_back_repository.project.domain.recipe.dto.RecipeGenerateRequest;
import com.team_5_back_repository.project.domain.recipe.dto.RecipeResponse;
import com.team_5_back_repository.project.domain.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    @PostMapping("/generate")
    public ResponseEntity<List<RecipeResponse>> generate(
            @RequestBody RecipeGenerateRequest req) throws Exception{
        return ResponseEntity.ok(recipeService.generateRecipes(req));
    }
}
