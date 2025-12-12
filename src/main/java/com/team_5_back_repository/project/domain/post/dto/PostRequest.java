package com.team_5_back_repository.project.domain.post.dto;

import com.team_5_back_repository.project.domain.post.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class PostRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private PostType postType;
    private Set<String> tags;
    private List<MultipartFile> files;
    private List<String> remainFileUrls;
}
