package com.team_5_back_repository.project.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team_5_back_repository.project.domain.member.dto.dto.RegionDto;
import com.team_5_back_repository.project.domain.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
    @Size(min = 8, max = 13, message = "비밀번호는 8자 이상 13자 이하로 입력해 주세요.")
    String password;
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(min = 2, max = 8, message = "닉네임은 2~8자여야 합니다.")
    String nickname;
    @NotEmpty(message = "활동 지역은 최소 한 개 이상 선택해야 합니다.")
    List<RegionDto> regions;
    @JsonProperty(defaultValue = "")
    @Size(max = 200, message = "소개는 200자 이하로 입력해주세요.")
    String introduction;
    public Member toEntity() {
        return Member.builder()
                .email(this.email)
                .password(this.password)
                .nickname(this.nickname)
                .introduction(this.introduction)
                .apiKey(UUID.randomUUID().toString())
                .activityRegions(new ArrayList<>())
                .build();
    }
}
