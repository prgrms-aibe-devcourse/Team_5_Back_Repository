package com.team_5_back_repository.project.domain.review.repository;

import com.team_5_back_repository.project.domain.review.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @EntityGraph(attributePaths = {"restaurant", "member"})
    List<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    @EntityGraph(attributePaths = {"restaurant", "member"})
    Optional<Review> findByIdAndMemberId(Long id, Long memberId);
    @EntityGraph(attributePaths = {"restaurant", "member"})
    Optional<Review> findWithRelationsById(Long id);

    void deleteByRestaurantId(Long restaurantId);
}
