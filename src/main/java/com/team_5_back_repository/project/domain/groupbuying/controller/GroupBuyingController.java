package com.team_5_back_repository.project.domain.groupbuying.controller;

import com.team_5_back_repository.project.domain.groupbuying.dto.request.GroupBuyingCreateRequest;
import com.team_5_back_repository.project.domain.groupbuying.dto.request.GroupBuyingJoinRequest;
import com.team_5_back_repository.project.domain.groupbuying.dto.response.GroupBuyingListResponse;
import com.team_5_back_repository.project.domain.groupbuying.dto.response.GroupBuyingParticipantResponse;
import com.team_5_back_repository.project.domain.groupbuying.dto.response.GroupBuyingPostResponse;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingStatus;
import com.team_5_back_repository.project.domain.groupbuying.service.GroupBuyingService;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.global.cloudstorage.entity.FileEntity;
import com.team_5_back_repository.project.global.cloudstorage.service.StorageService;
import com.team_5_back_repository.project.global.security.Rq.Rq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/group-buying")
@RequiredArgsConstructor
public class GroupBuyingController {

    private final GroupBuyingService groupBuyingService;
    private final StorageService storageService;
    private final Rq rq;

    /**
     * 공동구매 이미지 업로드 (최대 1개)
     * 로그인 필수
     */
    @PostMapping("/images")
    public ResponseEntity<List<FileEntity>> uploadImages(
            @RequestParam("images") List<MultipartFile> images) {

        // 로그인 확인
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalArgumentException("로그인이 필요한 기능입니다.");
        }

        // 이미지 개수 검증 (최대 1개)
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지가 없습니다.");
        }

        if (images.size() > 1) {
            throw new IllegalArgumentException("이미지는 최대 1개까지 업로드 가능합니다.");
        }

        log.info("공동구매 이미지 업로드 시작: memberId={}, imageCount={}",
                actor.getId(), images.size());

        // S3 업로드
        List<FileEntity> uploadedFiles = storageService.multiUpload(images, "group-buying");

        log.info("공동구매 이미지 업로드 완료: memberId={}, uploadedCount={}",
                actor.getId(), uploadedFiles.size());

        return ResponseEntity.ok(uploadedFiles);
    }

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
     * 공동구매 상세 조회 (조회수 증가 O)
     * GET /api/v1/group-buying/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<GroupBuyingPostResponse> getPost(
            @PathVariable Long postId
    ) {
        log.info("게시글 상세 조회 (조회수 증가): postId={}", postId);
        GroupBuyingPostResponse response = groupBuyingService.getPost(postId);
        return ResponseEntity.ok(response);
    }

    /**
     * 공동구매 정보 조회 (조회수 증가 X)
     * GET /api/v1/group-buying/{postId}/info
     */
    @GetMapping("/{postId}/info")
    public ResponseEntity<GroupBuyingPostResponse> getPostInfo(
            @PathVariable Long postId
    ) {
        log.info("게시글 정보 조회 (조회수 증가 X): postId={}", postId);
        GroupBuyingPostResponse response = groupBuyingService.getPostInfo(postId);
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