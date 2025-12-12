package com.team_5_back_repository.project.domain.groupbuying.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyingListResponse {

    private List<GroupBuyingPostResponse> posts;
    private Integer totalCount;
}