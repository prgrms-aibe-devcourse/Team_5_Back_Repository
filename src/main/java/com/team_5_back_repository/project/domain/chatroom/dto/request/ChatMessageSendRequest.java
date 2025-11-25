package com.team_5_back_repository.project.domain.chatroom.dto.request;

import com.team_5_back_repository.project.domain.chatroom.entity.ChatMessage.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageSendRequest {

    @NotNull(message = "채팅방 ID는 필수입니다.")
    private Long chatRoomId;

    @NotNull(message = "메시지 타입은 필수입니다.")
    private MessageType type;  // ENTER, TALK, LEAVE

    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String content;
}