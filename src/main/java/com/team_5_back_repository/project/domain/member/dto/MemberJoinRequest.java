package com.team_5_back_repository.project.domain.member.dto;

import com.team_5_back_repository.project.domain.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Builder
@Data
public class MemberJoinRequest {
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    String email;
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    String password;
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    String nickname;
    @NotEmpty(message = "활동 지역은 최소 한 개 이상 선택해야 합니다.")
    List<RegionDto> regions;
    public Member toEntity() {
        return Member.builder()
                .email(this.email)
                .password(this.password)
                .nickname(this.nickname)
                .apiKey(UUID.randomUUID().toString())
                .activityRegions(new ArrayList<>())
                .build();
    }
}
