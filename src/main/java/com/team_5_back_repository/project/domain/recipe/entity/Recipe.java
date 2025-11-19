package com.team_5_back_repository.project.domain.recipe.entity;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.recipe.enums.CookingTime;
import com.team_5_back_repository.project.domain.recipe.enums.Difficulty;
import com.team_5_back_repository.project.domain.recipe.enums.RecipeCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "recipe")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private RecipeCategory category;

    @Enumerated(EnumType.STRING)
    private CookingTime cookingTime;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private int servings; // 인분 수

    @Column(columnDefinition = "TEXT")
    private String ingredients; // JSON 문자열

    @Column(columnDefinition = "TEXT")
    private String steps; // JSON 문자열
}
