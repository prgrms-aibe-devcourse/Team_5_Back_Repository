package com.team_5_back_repository.project.domain.chatroom.dto.request;

import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomCreateRequest {

    @NotBlank(message = "채팅방 이름은 필수입니다.")
    private String name;

    @NotNull(message = "채팅방 타입은 필수입니다.")
    private ChatRoomType type;  // SMALL_GROUP or GROUP_PURCHASE

    @NotBlank(message = "지역은 필수입니다.")
    private String region;

    private String description;

    @NotNull(message = "최대 인원은 필수입니다.")
    @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
    @Max(value = 50, message = "최대 50명까지 가능합니다.")
    private Integer maxParticipants;

    @Schema(description = "카테고리 (맛집, 운동, 문화 등)")
    private String category;
}