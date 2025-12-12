package com.team_5_back_repository.project.domain.post.dto;

import com.team_5_back_repository.project.domain.post.entity.Post;
import com.team_5_back_repository.project.domain.post.entity.PostType;
import com.team_5_back_repository.project.domain.post.entity.Tag;
import com.team_5_back_repository.project.global.cloudstorage.entity.FileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private List<String> imageUrls;
    private String memberNickname;
    private Long memberId;
    private PostType postType;
    private Set<String> tags;
    private Long viewCount;
    private int likeCount;
    private int dislikeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isHot;
    private boolean isAuthor;
    private boolean isAdmin;
    private boolean isBookmarked;

    public static PostResponse from(Post post, Long currentMemberId, boolean isAdmin, boolean isBookmarked) {
        List<String> urls =  post.getAttachmentPath() == null
                ? new ArrayList<>()
                : post.getAttachmentPath()
                .stream()
                .map(FileEntity::getImgUrl)
                .toList();

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .memberNickname(post.getMember().getNickname())
                .memberId(post.getMember().getId())
                .postType(post.getPostType())
                .tags(post.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .dislikeCount(post.getDislikeCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .imageUrls(urls)
                .isAuthor(post.getMember().getId().equals(currentMemberId))
                .isAdmin(isAdmin)
                .isHot(post.isHot())
                .isBookmarked(isBookmarked)
                .build();
    }
}
