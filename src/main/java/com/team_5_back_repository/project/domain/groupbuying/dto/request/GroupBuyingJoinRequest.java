package com.team_5_back_repository.project.domain.groupbuying.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyingJoinRequest {

    @NotNull(message = "기여 금액은 필수입니다.")
    @Min(value = 0, message = "기여 금액은 0 이상이어야 합니다.")
    private Integer contributedAmount;
}