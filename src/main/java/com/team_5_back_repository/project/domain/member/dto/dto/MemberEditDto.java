package com.team_5_back_repository.project.domain.member.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEditDto {
    private String nickname;
    private String introduction;
    private List<RegionDto> regions;
    private String email;
}
