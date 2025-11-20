package com.team_5_back_repository.project.domain.review.service;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import com.team_5_back_repository.project.domain.restaurant.entity.Restaurant;
import com.team_5_back_repository.project.domain.restaurant.repository.RestaurantRepository;
import com.team_5_back_repository.project.domain.review.dto.ReviewDto;
import com.team_5_back_repository.project.domain.review.entity.Review;
import com.team_5_back_repository.project.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;

    public ReviewDto create(Long restaurantId, int rating, String content, Long memberId) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("rating must be 1~5");

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NoSuchElementException("restaurant not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("member not found"));

        Review review = Review.builder()
                .restaurant(restaurant)
                .member(member)
                .rating(rating)
                .content(content)
                .build();
        Review saved = reviewRepository.save(review);

        restaurant.applyNewRating(rating);

        return ReviewDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> listByRestaurant(Long restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream()
                .map(ReviewDto::from)
                .collect(Collectors.toList());
    }

    public ReviewDto update(Long reviewId, int rating, String content, Long memberId) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("rating must be 1~5");

        Review review = reviewRepository.findWithRelationsById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("review not found"));
        if (!review.getMember().getId().equals(memberId)) {
            throw new SecurityException("not owner");
        }

        int oldRating = review.getRating();
        review.update(rating, content);
        Restaurant restaurant = review.getRestaurant();
        restaurant.applyUpdatedRating(oldRating, rating);

        return ReviewDto.from(review);
    }

    public void delete(Long reviewId, Long memberId) {
        Review review = reviewRepository.findWithRelationsById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("review not found"));
        if (!review.getMember().getId().equals(memberId)) {
            throw new SecurityException("not owner");
        }
        int oldRating = review.getRating();
        Restaurant restaurant = review.getRestaurant();
        reviewRepository.delete(review);
        restaurant.applyDeletedRating(oldRating);
    }
}
