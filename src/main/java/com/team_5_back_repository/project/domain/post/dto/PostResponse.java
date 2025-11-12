package com.team_5_back_repository.project.domain.post.dto;

import com.team_5_back_repository.project.domain.post.entity.PostType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String attachmentPath;
    private PostType postType;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
