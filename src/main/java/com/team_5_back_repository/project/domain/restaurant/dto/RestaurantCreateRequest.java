package com.team_5_back_repository.project.domain.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RestaurantCreateRequest(
        @NotBlank String name,
        @NotBlank String jibunAddress,
        @NotBlank String roadAddress,
        String phone,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
