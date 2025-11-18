package com.team_5_back_repository.project.domain.member.service;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.global.standard.util.Ut;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
    @Value("${spring.jwt.secret}")
    private String jwtSecretKey;

    @Value("${spring.jwt.expiration}")
    private int accessTokenExpireSeconds;

    String genAccessToken(Member member) {
        long id = member.getId();
        String email = member.getEmail();
        String nickname = member.getNickname();

        Map<String, Object> claims = Map.of("id", id, "email", email, "nickname", nickname);

        return Ut.jwt.toString(
                jwtSecretKey,
                accessTokenExpireSeconds,
                claims
        );
    }

    Map<String, Object> payload(String accessToken) {
        Map<String, Object> parsedPayload = Ut.jwt.payload(jwtSecretKey, accessToken);

        if (parsedPayload == null) return null;

        long id = ((Number) parsedPayload.get("id")).longValue();

        String email = (String) parsedPayload.get("email");

        String nickname = (String) parsedPayload.get("nickname");

        return Map.of("id", id, "email", email, "nickname", nickname);
    }
}
