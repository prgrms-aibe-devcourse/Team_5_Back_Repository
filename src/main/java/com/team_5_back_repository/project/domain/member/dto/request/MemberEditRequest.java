package com.team_5_back_repository.project.domain.member.dto.request;

import com.team_5_back_repository.project.domain.member.dto.dto.RegionDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data

public class MemberEditRequest {
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자여야 합니다.")
    String nickname;
    @NotEmpty(message = "활동 지역은 최소 한 개 이상 선택해야 합니다.")
    List<RegionDto> regions;
    @Size(max = 200, message = "소개는 200자 이하로 입력해주세요.")
    String introduction;
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    String email;
}