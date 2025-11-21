package com.team_5_back_repository.project.domain.member.dto.request;

import com.team_5_back_repository.project.domain.member.dto.dto.RegionDto;
import lombok.Data;

import java.util.List;

@Data

public class MemberEditRequest {
    String nickname;
    List<RegionDto> regions;
    String introduction;
    String email;
}