package com.team_5_back_repository.project.domain.member.dto.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberStatDto {
    private Long postCount;
    private Long commentCount;
    private Long bookmarkCount;
    private Long followingCount;
    private Long followerCount;
}
