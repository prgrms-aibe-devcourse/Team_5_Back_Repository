package com.team_5_back_repository.project.global.security.Rq;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.exception.MemberException;
import com.team_5_back_repository.project.domain.member.service.MemberService;
import com.team_5_back_repository.project.global.security.SecurityUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest req;
    private final HttpServletResponse resp;
    private final MemberService memberService;

    public Member getActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUser)) return null;
        SecurityUser user = (SecurityUser) auth.getPrincipal();
        return Member.builder()
                .id(user.getId())
                .email(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }


    public void setHeader(String name, String value) {
        if (value == null) value = "";

        if (value.isBlank()) {
            req.removeAttribute(name);
        } else {
            resp.setHeader(name, value);
        }
    }

    public String getHeader(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getHeader("Authorization"))
                .filter(headerValue -> !headerValue.isBlank())
                .orElse(defaultValue);
    }

    public String getCookieValue(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getCookies())
                .flatMap(
                        cookies ->
                                Arrays.stream(req.getCookies())
                                        .filter(cookie -> name.equals(cookie.getName()))
                                        .map(Cookie::getValue)
                                        .findFirst()
                )
                .orElse(defaultValue);
    }

    public void setCookie(String name, String value) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // 쿠키를 도메인 전체에서 쓰겠다.
        cookie.setHttpOnly(false); // 쿠키를 스크립트로 접근 못하게(XSS 공격방어)
        // cookie.setDomain("localhost"); // 쿠키가 적용될 도메인 지정
        // cookie.setSecure(false); // http에서도 작동 (https일 때는 true) TODO 배포 시 true로 변경 필요
        // cookie.setAttribute("SameSite", "Strict"); // 크로스 사이트 요청 위조 방지
        cookie.setDomain("localhost"); // 쿠키가 적용될 도메인 지정
        cookie.setSecure(false); // http에서도 작동 (https일 때는 true) TODO 배포 시 true로 변경 필요
        cookie.setAttribute("SameSite", "Lax"); // 크로스 사이트 요청 위조 방지

        if (value.isBlank()) {
            cookie.setMaxAge(0);
        } else {
            cookie.setMaxAge(60 * 60 * 24 * 7); // 유효기간 1주일
        }

        resp.addCookie(cookie);
    }

    public void deleteCookie(String name) {
        setCookie(name, null);
    }

    @SneakyThrows
    public void sendRedirect(String url) {
        resp.sendRedirect(url);
    }

    public Member getActorFromDb() {
        Member actor = getActor();

        if (actor == null) {
            return null;
        }

        return memberService.findById(actor.getId()).orElseThrow(() ->
                new MemberException("404-1", "존재하지 않는 회원입니다."));
    }
}
