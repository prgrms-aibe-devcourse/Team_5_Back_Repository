package com.team_5_back_repository.project.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberLoginRequest {
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    String email;
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    String password;
}
