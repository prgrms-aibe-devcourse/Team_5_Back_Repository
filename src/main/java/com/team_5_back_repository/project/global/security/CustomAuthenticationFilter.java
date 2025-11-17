package com.team_5_back_repository.project.global.security;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.exception.MemberException;
import com.team_5_back_repository.project.domain.member.service.MemberService;
import com.team_5_back_repository.project.global.rsData.RsData;
import com.team_5_back_repository.project.global.security.Rq.Rq;
import com.team_5_back_repository.project.global.standard.util.Ut;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final MemberService memberService;
    private final Rq rq;

    private List<String> excludedUrls = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/signup"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, ServletException {

        try {
            // API 요청이 아니거나 인증 필요 없는 URL이면 패스
            if (!request.getRequestURI().startsWith("/api/") || excludedUrls.contains(request.getRequestURI())) {
                filterChain.doFilter(request, response);
                return;
            }

            String apiKey = "";
            String accessToken = "";

            // 헤더 우선, 없으면 쿠키에서 추출
            String headerAuthorization = rq.getHeader("Authorization", "");
            if (!headerAuthorization.isBlank()) {
                if (!headerAuthorization.startsWith("Bearer ")) {
                    throw new MemberException("401-2", "인증 정보가 올바르지 않습니다.");
                }
                String[] parts = headerAuthorization.split(" ", 3);
                apiKey = parts[1];
                accessToken = parts.length == 3 ? parts[2] : "";
            } else {
                apiKey = rq.getCookieValue("apiKey", "");
                accessToken = rq.getCookieValue("accessToken", "");
            }

//            log.debug("apiKey: " + apiKey);
//            log.debug("accessToken: " + accessToken);

            if (apiKey.isBlank() && accessToken.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            Member member = null;
            boolean isAccessTokenValid = false;

            if (!accessToken.isBlank()) {
                Map<String, Object> payload = memberService.payload(accessToken);
                if (payload != null) {
                    long id = ((Number) payload.get("id")).longValue();
                    String email = (String) payload.get("email");
                    String nickname = (String) payload.get("nickname");
                    member = Member.builder()
                            .id(id)
                            .email(email)
                            .nickname(nickname)
                            .build();
                    isAccessTokenValid = true;
                }
            }

            // accessToken 없거나 유효하지 않으면 apiKey로 member 조회
            if (member == null) {
                member = memberService.findByApiKey(apiKey)
                        .orElseThrow(() -> new MemberException("401-3", "회원을 찾을 수 없습니다."));
            }

            // accessToken 유효하지 않을 때 재발급
            if (!isAccessTokenValid && !accessToken.isBlank()) {
                String newAccessToken = memberService.genAccessToken(member);
                rq.setCookie("accessToken", newAccessToken);
            }

            UserDetails user = new SecurityUser(
                    member.getId(),
                    member.getEmail(),
                    "",
                    member.getNickname(),
                    member.getAuthorities()
            );

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    "",
                    user.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (MemberException e) {
            RsData<Void> rsData = e.getRsData();
            response.setContentType("application/json");
            response.setStatus(rsData.statusCode());
            response.getWriter().write(Ut.json.toString(rsData));
        }
    }
}

