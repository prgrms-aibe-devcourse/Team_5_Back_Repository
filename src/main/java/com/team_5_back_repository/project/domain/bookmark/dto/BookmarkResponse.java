package com.team_5_back_repository.project.domain.bookmark.dto;

import com.team_5_back_repository.project.domain.bookmark.entity.Bookmark;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkResponse {
    private Long id;
    private Long memberId;
    private String type;
    private Long targetId;

    public static BookmarkResponse from(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .memberId(bookmark.getMember().getId())
                .type(bookmark.getBookmarkType().name())
                .targetId(bookmark.getTargetId())
                .build();
    }
}
