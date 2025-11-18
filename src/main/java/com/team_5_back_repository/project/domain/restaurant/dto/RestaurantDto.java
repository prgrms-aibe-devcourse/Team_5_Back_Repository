package com.team_5_back_repository.project.domain.restaurant.dto;

import com.team_5_back_repository.project.domain.restaurant.entity.Restaurant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RestaurantDto {
    private final Long id;
    private final String name;
    private final String phone;
    private final String jibunAddress;
    private final String roadAddress;
    private final double latitude;
    private final double longitude;
    private final double distanceKm;
    private final double averageRating;
    private final long reviewCount;

    public static RestaurantDto of(Restaurant r, double distanceKm) {
        return new RestaurantDto(
                r.getId(),
                r.getName(),
                r.getPhone(),
                r.getJibunAddress(),
                r.getRoadAddress(),
                r.getLatitude(),
                r.getLongitude(),
                distanceKm,
                r.getAverageRating(),
                r.getReviewCount()
        );
    }
}
