package com.team_5_back_repository.project.domain.member.dto.dto;

import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GroupChatDto {
    private Long chatRoomId;
    private String name;
    private Integer currentParticipants;
    private Integer maxParticipants;
    private LocalDateTime createdAt;
}
