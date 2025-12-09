package com.team_5_back_repository.project.domain.groupbuying.dto.response;

import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyingParticipantResponse {

    private Long id;
    private Long groupBuyingPostId;
    private Long memberId;
    private String memberNickname;
    private Integer contributedAmount;
    private LocalDateTime joinedAt;

    public static GroupBuyingParticipantResponse from(GroupBuyingParticipant participant) {
        return GroupBuyingParticipantResponse.builder()
                .id(participant.getId())
                .groupBuyingPostId(participant.getGroupBuyingPostId())
                .memberId(participant.getMemberId())
                .memberNickname(participant.getMemberNickname())
                .contributedAmount(participant.getContributedAmount())
                .joinedAt(participant.getJoinedAt())
                .build();
    }
}