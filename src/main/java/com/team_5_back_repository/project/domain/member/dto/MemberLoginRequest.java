package com.team_5_back_repository.project.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberLoginRequest {
    @NotBlank
    String email;
    @NotBlank
    String password;
}
