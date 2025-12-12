package com.team_5_back_repository.project.domain.recipe.repository;

import com.team_5_back_repository.project.domain.recipe.entity.Recipe;
import com.team_5_back_repository.project.domain.recipe.enums.RecipeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    // 회원이 생성한 모든 레시피 (사이드바)
    List<Recipe> findAllByMemberIdAndStatusOrderByIdDesc(Long memberId, RecipeStatus status);

    // 회원이 저장한 레시피만 (마이페이지)
    List<Recipe> findAllByMemberIdAndStatus(Long memberId, RecipeStatus status);

    // 공유 토큰으로 레시피 조회
    Recipe findByShareToken(String shareToken);
}