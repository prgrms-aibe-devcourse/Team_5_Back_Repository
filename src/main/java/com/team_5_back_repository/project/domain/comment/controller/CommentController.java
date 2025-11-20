package com.team_5_back_repository.project.domain.comment.controller;

import com.team_5_back_repository.project.domain.comment.dto.CommentRequest;
import com.team_5_back_repository.project.domain.comment.dto.CommentResponse;
import com.team_5_back_repository.project.domain.comment.entity.Comment;
import com.team_5_back_repository.project.domain.comment.service.CommentService;
import com.team_5_back_repository.project.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comment/{postId}")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public RsData<Long> createComment(
            @PathVariable Long postId,
            @RequestParam Long memberId,
            @RequestBody CommentRequest request
    ) {
        Long id = commentService.createComment(postId, memberId, request);
        return new RsData<>("S-1", "댓글 작성 완료", id);
    }

    @GetMapping
    public RsData<List<CommentResponse>> getComments(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getComments(postId);
        return new RsData<>("S-1", "댓글 리스트 조회 성공", comments);
    }

    @PutMapping("/{commentId}")
    public RsData<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestParam Long memberId,
            @RequestBody CommentRequest request
    ) {
        CommentResponse updated = commentService.updateComment(commentId, memberId, request);
        return new RsData<>("S-1", "댓글 수정 완료", updated);
    }

    @DeleteMapping("/{commentId}")
    public RsData<?> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long memberId
    ) {
        commentService.deleteComment(commentId, memberId);
        return new RsData<>("S-1", "댓글 삭제 완료", null);
    }
}
