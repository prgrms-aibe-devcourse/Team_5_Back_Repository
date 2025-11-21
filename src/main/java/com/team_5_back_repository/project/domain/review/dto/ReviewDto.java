package com.team_5_back_repository.project.domain.review.dto;

import com.team_5_back_repository.project.domain.review.entity.Review;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ReviewDto {
    private final Long id;
    private final Long restaurantId;
    private final Long memberId;
    private final String memberNickname;
    private final int rating;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static ReviewDto from(Review r) {
        return new ReviewDto(
                r.getId(),
                r.getRestaurant().getId(),
                r.getMember().getId(),
                r.getMember().getNickname(),
                r.getRating(),
                r.getContent(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
