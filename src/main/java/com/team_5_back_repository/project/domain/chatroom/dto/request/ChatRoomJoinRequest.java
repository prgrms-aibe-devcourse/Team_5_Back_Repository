package com.team_5_back_repository.project.domain.chatroom.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomJoinRequest {

    @NotNull(message = "채팅방 ID는 필수입니다.")
    private Long chatRoomId;
}