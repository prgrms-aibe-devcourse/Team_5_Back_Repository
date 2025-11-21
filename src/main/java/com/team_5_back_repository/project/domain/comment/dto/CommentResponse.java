package com.team_5_back_repository.project.domain.comment.dto;

import com.team_5_back_repository.project.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private Long postId;
    private Long memberId;
    private String memberNickname;
    private String content;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getContent() {
        return Optional.of(deleted)
                .filter(d -> d)
                .map(d -> "삭제된 댓글입니다.")
                .orElse(content);
    }

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .memberId(comment.getMember().getId())
                .memberNickname(comment.getMember().getNickname())
                .content(comment.getContent())
                .deleted(comment.isDeleted())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
