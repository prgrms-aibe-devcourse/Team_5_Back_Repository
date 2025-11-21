package com.team_5_back_repository.project.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequest {
    @Min(1)
    @Max(5)
    private int rating;
    @NotBlank
    private String content;
}

