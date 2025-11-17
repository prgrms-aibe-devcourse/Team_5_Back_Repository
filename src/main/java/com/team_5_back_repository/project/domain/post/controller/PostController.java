package com.team_5_back_repository.project.domain.post.controller;

import com.team_5_back_repository.project.domain.post.dto.PostRequest;
import com.team_5_back_repository.project.domain.post.dto.PostResponse;
import com.team_5_back_repository.project.domain.post.entity.PostType;
import com.team_5_back_repository.project.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/onelife")
@RequiredArgsConstructor
@Tag(name = "Post", description = "게시글 API")
public class PostController {

    private final PostService postService;

    // 생성
    @PostMapping
    public ResponseEntity<Long> createPost(
            @RequestBody PostRequest request
    ) {
        Long id = postService.createPost(request,  "username");
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean increaseView
    ) {
        PostResponse response = postService.getPost(id, increaseView);
        return ResponseEntity.ok(response);
    }

    // 조회
    @GetMapping
    public ResponseEntity<Page<PostResponse>> listPosts(
            @RequestParam PostType type,
            Pageable pageable
    ) {
        Page<PostResponse> response = postService.listPosts(type, pageable);
        return ResponseEntity.ok(response);
    }

    // 업데이트
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PostRequest request

    ) {
        return ResponseEntity.ok(
                postService.updatePost(id, request, "username")
        );
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id

    ) {
        postService.deletePost(id, "username");
        return ResponseEntity.noContent().build();
    }
}
