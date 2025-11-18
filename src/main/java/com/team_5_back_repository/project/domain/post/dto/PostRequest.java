package com.team_5_back_repository.project.domain.post.dto;

import com.team_5_back_repository.project.domain.post.entity.PostType;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PostRequest {
    private String title;
    private String content;
    private String attachmentPath;
    private PostType postType;
    private Set<String> tags;
}
