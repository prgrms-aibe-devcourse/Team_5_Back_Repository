package com.team_5_back_repository.project.domain.review.repository;

import com.team_5_back_repository.project.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    Optional<Review> findByIdAndMemberId(Long id, Long memberId);
}
