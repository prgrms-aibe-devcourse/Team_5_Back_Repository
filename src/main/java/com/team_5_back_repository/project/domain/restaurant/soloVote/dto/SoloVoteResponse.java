package com.team_5_back_repository.project.domain.restaurant.soloVote.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SoloVoteResponse {
    private long yesCount;
    private long noCount;
    private Boolean myChoice; // null if not voted, true/false otherwise
}
