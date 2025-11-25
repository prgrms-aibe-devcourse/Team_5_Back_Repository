package com.team_5_back_repository.project.domain.member.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class MyPageDto {
    private String nickname;
    private List<String> regions;
    private String introduction;
    //private String profileImageUrl; TODO: 프로필 이미지 추가 필요
    private String joinDate;
    private MemberStatDto stats;
}
