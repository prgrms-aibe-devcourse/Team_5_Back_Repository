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
    private long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isHot;
    private boolean isAuthor;
    private boolean isAdmin;
    private boolean isBookmarked;

    public PostResponse(
            Long id,
            String title,
            String content,
            Long viewCount,
            int likeCount,
            int dislikeCount,
            long commentCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean isHot
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isHot = isHot;
    }

    public static PostResponse from(
            Post post,
            Long currentMemberId,
            boolean isAdmin,
            boolean isBookmarked,
            long commentCount
    ) {
        List<String> urls = post.getAttachmentPath()
                .stream()
                .map(FileEntity::getImgUrl)
                .toList();

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrls(urls)
                .memberNickname(post.getMember().getNickname())
                .memberId(post.getMember().getId())
                .postType(post.getPostType())
                .tags(post.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .dislikeCount(post.getDislikeCount())
                .commentCount(commentCount)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isHot(post.isHot())
                .isAuthor(post.getMember().getId().equals(currentMemberId))
                .isAdmin(isAdmin)
                .isBookmarked(isBookmarked)
                .build();
    }
}
