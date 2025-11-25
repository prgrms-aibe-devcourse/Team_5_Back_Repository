package com.team_5_back_repository.project.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    @Size(min = 8, max = 13, message = "비밀번호는 8자 이상 13자 이하로 입력해 주세요.")
    String currentPassword;
    @NotBlank
    @Size(min = 8, max = 13, message = "비밀번호는 8자 이상 13자 이하로 입력해 주세요.")
    String newPassword;
    @NotBlank
    @Size(min = 6, max = 6, message = "인증 코드는 6자여야 합니다.")
    Integer authCode;
}
