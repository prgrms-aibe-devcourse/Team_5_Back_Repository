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
    private String creatorNickname;
    private String region;
    private String description;
    private String category;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // 방장 닉네임을 포함한 from 메서드
    public static ChatRoomResponse from(ChatRoom chatRoom, String creatorNickname) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .type(chatRoom.getType())
                .creatorId(chatRoom.getCreatorId())
                .creatorNickname(creatorNickname)
                .region(chatRoom.getRegion())
                .description(chatRoom.getDescription())
                .category(chatRoom.getCategory())
                .maxParticipants(chatRoom.getMaxParticipants())
                .currentParticipants(chatRoom.getCurrentParticipants())
                .isActive(chatRoom.getIsActive())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    // 기존 호환성을 위한 from 메서드 (deprecated)
    @Deprecated
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .type(chatRoom.getType())
                .creatorId(chatRoom.getCreatorId())
                .creatorNickname("알 수 없음")
                .region(chatRoom.getRegion())
                .description(chatRoom.getDescription())
                .category(chatRoom.getCategory())
                .maxParticipants(chatRoom.getMaxParticipants())
                .currentParticipants(chatRoom.getCurrentParticipants())
                .isActive(chatRoom.getIsActive())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}