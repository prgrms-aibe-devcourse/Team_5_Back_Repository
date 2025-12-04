package com.team_5_back_repository.project.domain.post.controller;

import com.team_5_back_repository.project.domain.post.dto.PostRequest;
import com.team_5_back_repository.project.domain.post.dto.PostResponse;
import com.team_5_back_repository.project.domain.post.entity.PostType;
import com.team_5_back_repository.project.domain.post.service.PostService;
import com.team_5_back_repository.project.domain.post.util.SecurityUtil;
import com.team_5_back_repository.project.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/posts/onelife")
@RequiredArgsConstructor
@Tag(name = "Post-Controller", description = "게시글 API")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "게시글 작성",
            description = "새로운 게시글 생성 (팁 게시판은 관리자 only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공")
    })
    public ResponseEntity<RsData<Long>> createPost(@Valid @RequestBody PostRequest request) {
        Long memberId = SecurityUtil.getCurrentUserId();
        Long id = postService.createPost(request,  memberId);
        return ResponseEntity.ok( new RsData<>("200-1", "게시글 작성 성공", id));
    }

    @GetMapping("/{id}")
    @Operation( summary = "게시글 조회",
            description = "ID로 게시글 조회")
    public ResponseEntity<RsData<PostResponse>> getPost(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean increaseView
    ) {
        Long memberId = SecurityUtil.getCurrentUserId();
        PostResponse response = postService.getPost(id, increaseView, memberId);
        return ResponseEntity.ok( new RsData<>("200-1", "조회 성공", response));
    }

    @GetMapping
    @Operation( summary = "게시글 목록 조회",
            description = "게시글 타입별 목록 조회 (페이징 처리 가능)")
    public ResponseEntity<RsData<Page<PostResponse>>> listPosts(
            @RequestParam PostType type,
            Pageable pageable

    ) {
        Page<PostResponse> response;

        if (type == PostType.ALL) {
            response = postService.listAllPosts(pageable);
        } else {
            response = postService.listPosts(type, pageable);
        }
        return ResponseEntity.ok(new RsData<>("200-1", "목록 조회 성공", response));
    }

    @PutMapping("/{id}")
    @Operation( summary = "게시글 수정",
            description = "작성자만 게시글 수정")
    public ResponseEntity<RsData<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request
    ) {
        Long memberId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(
                new RsData<>("200-1", "수정 성공",postService.updatePost(id, request, memberId))
        );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }
}
