package com.team_5_back_repository.project.domain.groupbuying.dto.response;

import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingPost;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyingPostResponse {

    private Long id;
    private Long chatRoomId;
    private Long creatorId;
    private String title;
    private String category;
    private String content;
    private Integer targetAmount;
    private Integer currentAmount;
    private Integer targetParticipants;
    private Integer currentParticipants;
    private LocalDateTime deadline;
    private GroupBuyingStatus status;
    private String region;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer progressPercentage;
    private Boolean isExpired;
    private Long viewCount;
    private Long chatRoomMessageCount;
    private List<String> images;
    private String creatorNickname;

    public static GroupBuyingPostResponse from(GroupBuyingPost post) {
        int progressPercentage = 0;
        if (post.getTargetAmount() > 0) {
            progressPercentage = (post.getCurrentAmount() * 100) / post.getTargetAmount();
        }

        return GroupBuyingPostResponse.builder()
                .id(post.getId())
                .chatRoomId(post.getChatRoomId())
                .creatorId(post.getCreatorId())
                .title(post.getTitle())
                .category(post.getCategory())
                .content(post.getContent())
                .targetAmount(post.getTargetAmount())
                .currentAmount(post.getCurrentAmount())
                .targetParticipants(post.getTargetParticipants())
                .currentParticipants(post.getCurrentParticipants())
                .deadline(post.getDeadline())
                .status(post.getStatus())
                .region(post.getRegion())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .progressPercentage(progressPercentage)
                .isExpired(post.isExpired())
                .viewCount(post.getViewCount() != null ? post.getViewCount() : 0L)
                .images(post.getImageList())
                .chatRoomMessageCount(0L)
                .creatorNickname(null)
                .build();
    }

    public void setChatRoomMessageCount(Long count) {
        this.chatRoomMessageCount = count;
    }

    public void setCreatorNickname(String nickname) {
        this.creatorNickname = nickname;
    }
}