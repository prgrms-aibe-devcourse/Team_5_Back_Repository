package com.team_5_back_repository.project.global.security;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class CustomOAuth2LoginService extends DefaultOAuth2UserService {
    private final MemberService memberService;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String oauthUserId = "";
        String providerTypeCode = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        String nickname = "";
        String username = "";

        oauthUserId =  oAuth2User.getName();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> attributesProperties = (Map<String, Object>) attributes.get("properties");

        nickname = (String) attributesProperties.get("nickname");

        username = providerTypeCode + "__%s".formatted(oauthUserId);
        String password = "";

        Member member = memberService.joinOrModify(username, password, nickname);

        return new SecurityUser(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                member.getNickname(),
                member.getAuthorities()
        );
    }
}