package com.team_5_back_repository.project.domain.post.dto;

import com.team_5_back_repository.project.domain.post.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class PostRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private String attachmentPath;
    private PostType postType;
    private Set<String> tags;
}
