package com.team_5_back_repository.project.domain.like.controller;


import com.team_5_back_repository.project.domain.like.entity.ReactionType;
import com.team_5_back_repository.project.domain.like.service.LikeService;
import com.team_5_back_repository.project.global.rsData.RsData;
import com.team_5_back_repository.project.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
@RequiredArgsConstructor
@Tag(name = "Post-Reaction", description = "게시글 추천/비추천 API")
public class LikeController {

    private final LikeService likeService;

    private Long getCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser user = (SecurityUser) auth.getPrincipal();
        return user.getId();
    }
    @PostMapping("/like")
    @Operation(summary = "좋아요 등록/취소", description = "좋아요 등록 및 해제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 성공 또는 취소"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 회원을 찾을 수 없음"),
    })
    public RsData<?> like(@PathVariable Long postId) {

        Long memberId = getCurrentMemberId();

        ReactionType result = likeService.react(postId, memberId, ReactionType.LIKE);

        if (result == null)
            return new RsData<>("S-2", "좋아요 취소", null);

        return new RsData<>("S-1", "좋아요 성공", result);
    }

    @PostMapping("/dislike")
    @Operation(summary = "비추천 등록/취소", description = "비추천 등록 및 해제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비추천 성공 또는 취소"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 회원을 찾을 수 없음"),
    })
    public RsData<?> dislike(@PathVariable Long postId) {

        Long memberId = getCurrentMemberId();

        ReactionType result = likeService.react(postId, memberId, ReactionType.DISLIKE);

        if (result == null)
            return new RsData<>("S-2", "비추천 취소", null);

        return new RsData<>("S-1", "비추천 성공", result);
    }

    @GetMapping("/likes")
    @Operation(summary = "좋아요 개수 조회", description = "게시글의 좋아요 개수 조회")
    public RsData<?> countLikes(@PathVariable Long postId) {
        long count = likeService.countLikes(postId);
        return new RsData<>("S-1", "좋아요 수 조회", count);
    }

    @GetMapping("/dislikes")
    @Operation(summary = "비추천 개수 조회", description = "게시글의 비추천 개수 조회")
    public RsData<?> countDislikes(@PathVariable Long postId) {
        long count = likeService.countDislikes(postId);
        return new RsData<>("S-1", "비추천 수 조회", count);
    }

}
