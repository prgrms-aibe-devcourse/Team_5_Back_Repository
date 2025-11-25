package com.team_5_back_repository.project.domain.chatroom.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatRoomListResponse {

    private List<ChatRoomResponse> chatRooms;
    private Integer totalCount;
}