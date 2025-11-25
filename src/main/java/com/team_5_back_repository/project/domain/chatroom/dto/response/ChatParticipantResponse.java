package com.team_5_back_repository.project.domain.chatroom.dto.response;

import com.team_5_back_repository.project.domain.chatroom.entity.ChatParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ChatParticipantResponse {

    private Long memberId;
    private String nickname;
    private Boolean isCreator;
    private LocalDateTime joinedAt;

    public static ChatParticipantResponse from(ChatParticipant participant) {
        return ChatParticipantResponse.builder()
                .memberId(participant.getMemberId())
                .nickname(participant.getMemberNickname())
                .isCreator(participant.getIsCreator())
                .joinedAt(participant.getJoinedAt())
                .build();
    }
}