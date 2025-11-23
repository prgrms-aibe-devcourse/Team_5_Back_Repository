package com.team_5_back_repository.project.domain.like.controller;


import com.team_5_back_repository.project.domain.like.entity.ReactionType;
import com.team_5_back_repository.project.domain.like.service.LikeService;
import com.team_5_back_repository.project.global.rsData.RsData;
import com.team_5_back_repository.project.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    private Long getCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser user = (SecurityUser) auth.getPrincipal();
        return user.getId();
    }
    @PostMapping("/like")
    public RsData<?> like(@PathVariable Long postId) {

        Long memberId = getCurrentMemberId();

        ReactionType result = likeService.react(postId, memberId, ReactionType.LIKE);

        if (result == null)
            return new RsData<>("S-2", "좋아요 취소", null);

        return new RsData<>("S-1", "좋아요 성공", result);
    }
    @PostMapping("/dislike")
    public RsData<?> dislike(@PathVariable Long postId) {

        Long memberId = getCurrentMemberId();

        ReactionType result = likeService.react(postId, memberId, ReactionType.DISLIKE);

        if (result == null)
            return new RsData<>("S-2", "비추천 취소", null);

        return new RsData<>("S-1", "비추천 성공", result);
    }
    @GetMapping("/likes")
    public RsData<?> countLikes(@PathVariable Long postId) {
        long count = likeService.countLikes(postId);
        return new RsData<>("S-1", "좋아요 수 조회", count);
    }
    @GetMapping("/dislikes")
    public RsData<?> countDislikes(@PathVariable Long postId) {
        long count = likeService.countDislikes(postId);
        return new RsData<>("S-1", "비추천 수 조회", count);
    }

}
