package com.team_5_back_repository.project.global.security;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.service.MemberService;
import com.team_5_back_repository.project.global.security.Rq.Rq;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CustomOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler{
    private final MemberService memberService;
    private final Rq rq;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Member actor = rq.getActorFromDb();

        String accessToken = memberService.genAccessToken(actor);

        rq.setCookie("apiKey", actor.getApiKey());
        rq.setCookie("accessToken", accessToken);

        String redirectUrl = "/";

        String stateParam = request.getParameter("state");

        if (stateParam != null) {
            // 1️⃣ Base64 URL-safe 디코딩
            String decodedStateParam = new String(Base64.getUrlDecoder().decode(stateParam), StandardCharsets.UTF_8);

            // 2️⃣ '#' 앞은 redirectUrl, 뒤는 originState
            redirectUrl = decodedStateParam.split("#", 2)[0];
        }

        rq.sendRedirect(redirectUrl);
    }
}
