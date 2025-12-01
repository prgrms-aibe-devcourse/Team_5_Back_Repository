package com.team_5_back_repository.project.domain.chatroom.dto.response;

import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoom;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponse {

    private Long id;
    private String name;
    private ChatRoomType type;
    private Long creatorId;
    private String region;
    private String description;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .type(chatRoom.getType())
                .creatorId(chatRoom.getCreatorId())
                .region(chatRoom.getRegion())
                .description(chatRoom.getDescription())
                .maxParticipants(chatRoom.getMaxParticipants())
                .currentParticipants(chatRoom.getCurrentParticipants())
                .isActive(chatRoom.getIsActive())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}