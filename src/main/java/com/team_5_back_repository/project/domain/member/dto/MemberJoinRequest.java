package com.team_5_back_repository.project.domain.member.dto;

import com.team_5_back_repository.project.domain.member.entity.Member;
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
    @NotBlank
    String email;
    @NotBlank
    String password;
    @NotBlank
    String nickname;
    @NotEmpty
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
