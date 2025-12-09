package com.team_5_back_repository.project.domain.restaurant.soloVote.repository;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.restaurant.entity.Restaurant;
import com.team_5_back_repository.project.domain.restaurant.soloVote.entity.SoloVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SoloVoteRepository extends JpaRepository<SoloVote, Long> {
    Optional<SoloVote> findByMemberAndRestaurant(Member member, Restaurant restaurant);
    long countByRestaurantAndWillEatAlone(Restaurant restaurant, boolean willEatAlone);
    List<SoloVote> findByRestaurant(Restaurant restaurant);
    void deleteByMemberAndRestaurant(Member member, Restaurant restaurant);
}
