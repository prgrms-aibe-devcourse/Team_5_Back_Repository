package com.team_5_back_repository.project.domain.comment.controller;

import com.team_5_back_repository.project.domain.comment.dto.CommentRequest;
import com.team_5_back_repository.project.domain.comment.dto.CommentResponse;
import com.team_5_back_repository.project.domain.comment.service.CommentService;
import com.team_5_back_repository.project.global.rsData.RsData;
import com.team_5_back_repository.project.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comment/{postId}")
@Tag(name = "Comment-Controller", description = "게시글 댓글 API")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private Long getCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser user = (SecurityUser) auth.getPrincipal();
        return user.getId();
    }

    @PostMapping
    @Operation(summary = "댓글 작성", description = "게시글에 댓글 작성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 회원 없음")
    })
    public RsData<Long> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request
    ) {
        Long memberId = getCurrentMemberId();
        Long id = commentService.createComment(postId, memberId, request);
        return new RsData<>("200", "댓글 작성 완료", id);
    }

    @GetMapping
    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 리스트 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 리스트 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    public RsData<List<CommentResponse>> getComments(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getComments(postId);
        return new RsData<>("200", "댓글 리스트 조회 성공", comments);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "작성자 본인만 댓글 수정 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 완료"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "댓글 또는 회원 없음")
    })
    public RsData<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequest request
    ) {
        Long memberId = getCurrentMemberId();
        CommentResponse updated = commentService.updateComment(commentId, memberId, request);
        return new RsData<>("200", "댓글 수정 완료", updated);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "작성자와 관리자만 댓글 삭제 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 완료"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "댓글 또는 회원 없음")
    })
    public RsData<?> deleteComment(
            @PathVariable Long commentId
    ) {
        Long memberId = getCurrentMemberId();
        commentService.deleteComment(commentId, memberId);
        return new RsData<>("200", "댓글 삭제 완료", null);
    }
}
