package com.team_5_back_repository.project.domain.groupbuying.controller;

import com.team_5_back_repository.project.domain.groupbuying.dto.request.GroupBuyingCreateRequest;
import com.team_5_back_repository.project.domain.groupbuying.dto.request.GroupBuyingJoinRequest;
import com.team_5_back_repository.project.domain.groupbuying.dto.response.GroupBuyingListResponse;
import com.team_5_back_repository.project.domain.groupbuying.dto.response.GroupBuyingParticipantResponse;
import com.team_5_back_repository.project.domain.groupbuying.dto.response.GroupBuyingPostResponse;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingStatus;
import com.team_5_back_repository.project.domain.groupbuying.service.GroupBuyingService;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.global.security.Rq.Rq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/group-buying")
@RequiredArgsConstructor
public class GroupBuyingController {

    private final GroupBuyingService groupBuyingService;
    private final Rq rq;

    /**
     * 공동구매 게시글 생성 (로그인 필수)
     * POST /api/v1/group-buying
     */
    @PostMapping
    public ResponseEntity<GroupBuyingPostResponse> createPost(
            @Valid @RequestBody GroupBuyingCreateRequest request
    ) {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        GroupBuyingPostResponse response = groupBuyingService.createPost(request, actor.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 공동구매 목록 조회 (비회원도 가능)
     * GET /api/v1/group-buying?region=강남구&status=RECRUITING
     */
    @GetMapping
    public ResponseEntity<GroupBuyingListResponse> getPosts(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) GroupBuyingStatus status
    ) {
        GroupBuyingListResponse response = groupBuyingService.getPosts(region, status);
        return ResponseEntity.ok(response);
    }

    /**
     * 공동구매 상세 조회 (비회원도 가능)
     * GET /api/v1/group-buying/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<GroupBuyingPostResponse> getPost(
            @PathVariable Long postId
    ) {
        GroupBuyingPostResponse response = groupBuyingService.getPost(postId);
        return ResponseEntity.ok(response);
    }

    /**
     * 내가 참여한 공동구매 목록 (로그인 필수)
     * GET /api/v1/group-buying/my
     */
    @GetMapping("/my")
    public ResponseEntity<GroupBuyingListResponse> getMyPosts() {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        GroupBuyingListResponse response = groupBuyingService.getMyPosts(actor.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 공동구매 참여 (로그인 필수)
     * POST /api/v1/group-buying/{postId}/join
     */
    @PostMapping("/{postId}/join")
    public ResponseEntity<Void> joinPost(
            @PathVariable Long postId,
            @Valid @RequestBody GroupBuyingJoinRequest request
    ) {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        groupBuyingService.joinPost(postId, actor.getId(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 공동구매 나가기 (로그인 필수)
     * POST /api/v1/group-buying/{postId}/leave
     */
    @PostMapping("/{postId}/leave")
    public ResponseEntity<Void> leavePost(
            @PathVariable Long postId
    ) {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        groupBuyingService.leavePost(postId, actor.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * 공동구매 게시글 삭제 (로그인 필수)
     * DELETE /api/v1/group-buying/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId
    ) {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        groupBuyingService.deletePost(postId, actor.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * 공동구매 참여자 목록 조회 (비회원도 접근 가능, 비회원은 빈 목록)
     * GET /api/v1/group-buying/{postId}/participants
     */
    @GetMapping("/{postId}/participants")
    public ResponseEntity<List<GroupBuyingParticipantResponse>> getParticipants(
            @PathVariable Long postId
    ) {
        Member actor = rq.getActor();

        // 비회원이면 빈 목록 반환 (에러 없이)
        if (actor == null) {
            return ResponseEntity.ok(List.of());
        }

        List<GroupBuyingParticipantResponse> responses = groupBuyingService.getParticipants(postId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 공동구매 게시글 수정 (로그인 필수, 작성자만)
     * PUT /api/v1/group-buying/{postId}
     */
    @PutMapping("/{postId}")
    public ResponseEntity<GroupBuyingPostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody com.team_5_back_repository.project.domain.groupbuying.dto.request.GroupBuyingUpdateRequest request
    ) {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        GroupBuyingPostResponse response = groupBuyingService.updatePost(postId, request, actor.getId());
        return ResponseEntity.ok(response);
    }
}