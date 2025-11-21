package com.team_5_back_repository.project.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberLoginResponse {
    long id;
    String email;
    String nickname;
    String accessToken;
}
