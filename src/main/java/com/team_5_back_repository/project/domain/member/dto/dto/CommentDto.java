package com.team_5_back_repository.project.domain.member.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String postTitle;
    private String content;
    private LocalDateTime createdAt;
}
