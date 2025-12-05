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
            "/api/v1/auth/signup",
            "/api/v1/auth/check-nickname"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, ServletException {

        try {
            String requestURI = request.getRequestURI();
            String method = request.getMethod();

            // API 요청이 아니거나 인증 불필요한 경로면 패스
            if (!requestURI.startsWith("/api/") || shouldSkipAuthentication(requestURI, method)) {
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

            // ⭐ null 체크 추가
            String jsonResponse = Ut.json.toString(rsData);
            if (jsonResponse != null) {
                response.getWriter().write(jsonResponse);
            }
        }
    }

    /**
     * ⭐ 인증을 스킵할 경로인지 확인 (정밀한 제어)
     */
    private boolean shouldSkipAuthentication(String requestURI, String method) {
        // 정확히 일치하는 URL
        if (excludedUrls.contains(requestURI)) {
            return true;
        }

        // GET 요청 처리
        if ("GET".equals(method)) {
            // ⭐ 공동구매 참여자 조회는 인증 필요 (스킵 안 함)
            if (requestURI.matches("/api/v1/group-buying/\\d+/participants")) {
                return false; // 인증 필터 통과
            }

            // 공동구매 목록 조회 (비회원 허용)
            if (requestURI.equals("/api/v1/group-buying")) {
                return true;
            }

            // 공동구매 상세 조회 (비회원 허용)
            if (requestURI.matches("/api/v1/group-buying/\\d+")) {
                return true;
            }

            // ⭐ 채팅 메시지/참여자 조회는 인증 필요 (스킵 안 함)
            if (requestURI.contains("/chatrooms/") &&
                    (requestURI.contains("/messages") || requestURI.contains("/participants"))) {
                return false; // 인증 필터 통과
            }

            // 채팅방 목록/상세 조회 (비회원 허용)
            if (requestURI.startsWith("/api/chatrooms") ||
                    requestURI.startsWith("/api/v1/chatrooms")) {
                return true;
            }
        }

        // WebSocket
        if (requestURI.startsWith("/ws")) {
            return true;
        }

        // 지역 검색
        if (requestURI.equals("/api/v1/region/search")) {
            return true;
        }

        return false;
    }
}