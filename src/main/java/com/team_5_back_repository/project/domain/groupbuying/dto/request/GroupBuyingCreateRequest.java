package com.team_5_back_repository.project.domain.groupbuying.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyingCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이내여야 합니다.")
    private String title;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 2000, message = "내용은 2000자 이내여야 합니다.")
    private String content;

    @NotNull(message = "목표 금액은 필수입니다.")
    @Positive(message = "목표 금액은 0보다 커야 합니다.")
    private Integer targetAmount;

    @NotNull(message = "목표 인원은 필수입니다.")
    @Min(value = 2, message = "목표 인원은 최소 2명 이상이어야 합니다.")
    @Max(value = 100, message = "목표 인원은 최대 100명까지 가능합니다.")
    private Integer targetParticipants;

    @NotNull(message = "마감일은 필수입니다.")
    private LocalDateTime deadline;

    @NotBlank(message = "지역은 필수입니다.")
    private String region;

    @NotBlank(message = "채팅방 이름은 필수입니다.")
    private String chatRoomName;

    @Size(max = 500, message = "채팅방 설명은 500자 이내여야 합니다.")
    private String chatRoomDescription;

    @NotNull(message = "채팅방 최대 인원은 필수입니다.")
    @Min(value = 2, message = "채팅방 최대 인원은 최소 2명 이상이어야 합니다.")
    @Max(value = 100, message = "채팅방 최대 인원은 최대 100명까지 가능합니다.")
    private Integer chatRoomMaxParticipants;

    // 커스텀 검증 메서드 추가 (Service에서 호출)
    public void validateDeadline() {
        if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("마감일은 현재 시간 이후여야 합니다.");
        }
    }
}