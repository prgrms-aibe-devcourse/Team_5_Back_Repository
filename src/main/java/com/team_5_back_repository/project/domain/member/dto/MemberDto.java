package com.team_5_back_repository.project.domain.member.dto;

import com.team_5_back_repository.project.domain.member.entity.Member;

public record MemberDto(
        long id,
        String email,
        String nickname,
        String introduction
        //TODO 지역 추가 필요
) {
    public MemberDto(Member member) {
        this(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getIntroduction()
        );
    }
}
