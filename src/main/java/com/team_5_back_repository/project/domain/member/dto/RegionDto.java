package com.team_5_back_repository.project.domain.member.dto;

import com.team_5_back_repository.project.domain.member.entity.Region;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionDto {
    private String code;  // emd_cd
    private String full;  // full_nm
    private String small; // emd_kor_nm

    public Region toEntity() {
        return Region.builder()
                .id(Long.parseLong(this.code))
                .fullName(this.full)
                .shortName(this.small)
                .build();
    }
}
