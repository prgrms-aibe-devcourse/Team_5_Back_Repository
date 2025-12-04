package com.team_5_back_repository.project.domain.groupbuying.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyingUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @NotBlank(message = "지역은 필수입니다.")
    private String region;

    @NotNull(message = "마감일은 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadline;
}