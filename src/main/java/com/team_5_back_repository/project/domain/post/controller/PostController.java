package com.team_5_back_repository.project.domain.post.controller;

import com.team_5_back_repository.project.domain.post.dto.PostRequest;
import com.team_5_back_repository.project.domain.post.dto.PostResponse;
import com.team_5_back_repository.project.domain.post.entity.PostType;
import com.team_5_back_repository.project.domain.post.service.PostService;
import com.team_5_back_repository.project.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/onelife")
@RequiredArgsConstructor
@Tag(name = "Post-Controller", description = "게시글 API")
public class PostController {

    private final PostService postService;

    private Long getCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser user = (SecurityUser) auth.getPrincipal(); // SecurityUser에 id 포함
        return user.getId();
    }
    @PostMapping
    @Operation(summary = "게시글 작성",
            description = "새로운 게시글 생성 (팁 게시판은 관리자 only)")
    public ResponseEntity<Long> createPost(@Valid @RequestBody PostRequest request) {
        Long memberId = getCurrentMemberId();
        Long id = postService.createPost(request,  memberId);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    @Operation( summary = "게시글 조회",
            description = "ID로 게시글 조회")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean increaseView
    ) {
        PostResponse response = postService.getPost(id, increaseView);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    @Operation( summary = "게시글 목록 조회",
            description = "게시글 타입별 목록 조회 (페이징 처리 가능)")
    public ResponseEntity<Page<PostResponse>> listPosts(
            @RequestParam PostType type,
            Pageable pageable
    ) {
        Page<PostResponse> response = postService.listPosts(type, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation( summary = "게시글 수정",
            description = "작성자만 게시글 수정")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PostRequest request
    ) {
        Long memberId = getCurrentMemberId();
        return ResponseEntity.ok(postService.updatePost(id, request, memberId)
        );
    }

    @DeleteMapping("/{id}")
    @Operation( summary = "게시글 삭제",
            description = "작성자나 관리자만 삭제 가능")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        Long memberId = getCurrentMemberId();
        postService.deletePost(id,  memberId);
        return ResponseEntity.noContent().build();
    }
}
