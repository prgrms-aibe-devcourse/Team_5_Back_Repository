package com.team_5_back_repository.project.domain.member.dto;

import com.team_5_back_repository.project.domain.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberJoinRequest {
    @NotBlank
    String email;
    @NotBlank
    String password;
    @NotBlank
    String nickname;
    // TODO  지역 추가 필요
//    @NotBlank
//    String[] region;
    public Member toEntity() {
        return Member.builder()
                .email(this.email)
                .password(this.password)
                .nickname(this.nickname)
                .build();
    }
}
