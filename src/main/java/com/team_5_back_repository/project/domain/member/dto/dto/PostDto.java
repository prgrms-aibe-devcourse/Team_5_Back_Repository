package com.team_5_back_repository.project.domain.member.dto.dto;

import com.team_5_back_repository.project.domain.post.entity.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PostDto {
    private Long id;
    private String title;
    private PostType postType;
    private Long viewCount;
    private int likeCount;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
