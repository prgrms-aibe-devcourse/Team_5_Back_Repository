package com.team_5_back_repository.project.domain.recipe.enums;

public enum RecipeStatus {
    GUEST_GENERATED, // 비회원이 레시피를 생성함(비회원은 조회, 저장, 삭제 불가)
    GENERATED,   // 회원이 레시피를 생성하고, 저장하지 않음
    SAVED        // 회원이 레시피를 생성하고, 저장함
}
