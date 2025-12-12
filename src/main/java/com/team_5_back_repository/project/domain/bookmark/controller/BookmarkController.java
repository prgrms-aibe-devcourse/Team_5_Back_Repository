package com.team_5_back_repository.project.domain.bookmark.controller;

import com.team_5_back_repository.project.domain.bookmark.dto.BookmarkResponse;
import com.team_5_back_repository.project.domain.bookmark.entity.BookmarkType;
import com.team_5_back_repository.project.domain.bookmark.service.BookmarkService;
import com.team_5_back_repository.project.domain.post.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{type}/{targetId}")
    public ResponseEntity<BookmarkResponse> addBookmark(
            @PathVariable BookmarkType type,
            @PathVariable Long targetId
    ) {
        Long memberId = SecurityUtil.getCurrentUserId();
        BookmarkResponse response = bookmarkService.addBookmark(memberId, type, targetId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{type}/{targetId}")
    public ResponseEntity<Void> removeBookmark(
            @PathVariable BookmarkType type,
            @PathVariable Long targetId
    ) {
        Long memberId = SecurityUtil.getCurrentUserId();
        bookmarkService.removeBookmark(memberId, type, targetId);
        return ResponseEntity.noContent().build();
    }
}
