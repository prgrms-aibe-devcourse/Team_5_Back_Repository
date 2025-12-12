package com.team_5_back_repository.project.domain.restaurant.soloVote.service;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.restaurant.entity.Restaurant;
import com.team_5_back_repository.project.domain.restaurant.repository.RestaurantRepository;
import com.team_5_back_repository.project.domain.restaurant.soloVote.dto.SoloVoteResponse;
import com.team_5_back_repository.project.domain.restaurant.soloVote.entity.SoloVote;
import com.team_5_back_repository.project.domain.restaurant.soloVote.repository.SoloVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SoloVoteService {

    private final SoloVoteRepository soloVoteRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public SoloVoteResponse getSummary(Long restaurantId, Member member) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found"));

        long yes = soloVoteRepository.countByRestaurantAndWillEatAlone(restaurant, true);
        long no = soloVoteRepository.countByRestaurantAndWillEatAlone(restaurant, false);

        Boolean myChoice = null;
        if (member != null) {
            Optional<SoloVote> svOpt = soloVoteRepository.findByMemberAndRestaurant(member, restaurant);
            if (svOpt.isPresent()) myChoice = svOpt.get().isWillEatAlone();
        }

        return new SoloVoteResponse(yes, no, myChoice);
    }

    public SoloVoteResponse vote(Member member, Long restaurantId, boolean willEatAlone) {
        if (member == null) throw new IllegalArgumentException("member required");

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found"));

        Optional<SoloVote> existing = soloVoteRepository.findByMemberAndRestaurant(member, restaurant);

        if (existing.isPresent()) {
            SoloVote sv = existing.get();
            sv = SoloVote.builder()
                    .id(sv.getId())
                    .member(sv.getMember())
                    .restaurant(sv.getRestaurant())
                    .willEatAlone(willEatAlone)
                    .build();
            soloVoteRepository.save(sv);
        } else {
            SoloVote sv = SoloVote.builder()
                    .member(member)
                    .restaurant(restaurant)
                    .willEatAlone(willEatAlone)
                    .build();
            soloVoteRepository.save(sv);
        }

        return getSummary(restaurantId, member);
    }

    public SoloVoteResponse delete(Member member, Long restaurantId) {
        if (member == null) throw new IllegalArgumentException("member required");

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found"));

        soloVoteRepository.deleteByMemberAndRestaurant(member, restaurant);

        return getSummary(restaurantId, member);
    }
}
