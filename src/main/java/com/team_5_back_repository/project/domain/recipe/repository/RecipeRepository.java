package com.team_5_back_repository.project.domain.recipe.repository;

import com.team_5_back_repository.project.domain.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findAllByMemberIdOrderByIdDesc(Long memberId);
}
