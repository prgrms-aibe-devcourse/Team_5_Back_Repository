package com.team_5_back_repository.project.domain.chatroom.dto.response;

import com.team_5_back_repository.project.domain.chatroom.entity.ChatMessage;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatMessage.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {

    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String senderNickname;
    private MessageType type;
    private String content;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoomId())
                .senderId(message.getSenderId())
                .senderNickname(message.getSenderNickname())
                .type(message.getType())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}