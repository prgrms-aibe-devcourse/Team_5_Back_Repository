package com.team_5_back_repository.project.domain.restaurant.soloVote.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SoloVoteRequest {
    @NotNull
    private Boolean willEatAlone;
}
