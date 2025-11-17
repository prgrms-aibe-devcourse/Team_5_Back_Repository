package com.team_5_back_repository.project.domain.member.service;

import com.team_5_back_repository.project.domain.member.dto.MemberDto;
import com.team_5_back_repository.project.domain.member.dto.MemberJoinRequest;
import com.team_5_back_repository.project.domain.member.dto.MemberLoginRequest;
import com.team_5_back_repository.project.domain.member.dto.MemberLoginResponse;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.exception.MemberException;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final AuthTokenService authTokenService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public long countMembers() {
        return memberRepository.count();
    }

    @Transactional
    public MemberDto join(MemberJoinRequest memberJoinRequest) {
        memberRepository.findByNickname(memberJoinRequest.getEmail())
                .ifPresent(_member -> {
                    throw new MemberException("409-1", "이미 존재하는 회원입니다.");
                });
        memberJoinRequest.setPassword(passwordEncoder.encode(memberJoinRequest.getPassword()));
        Member savedMember = memberRepository.save(memberJoinRequest.toEntity());
        return new MemberDto(savedMember);
    }

    public Member login(MemberLoginRequest memberLoginRequest) {
        return memberRepository.findByEmail(memberLoginRequest.getEmail())
                .map(member -> {
                    if (passwordEncoder.matches(memberLoginRequest.getPassword(), member.getPassword())) {
                        return member;
                    } else {
                        throw new MemberException("402-1", "비밀번호가 일치하지 않습니다.");
                    }
                })
                .orElseThrow(() -> new MemberException("404-1", "존재하지 않는 회원입니다."));
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Map<String, Object> payload(String accessToken) {
        return authTokenService.payload(accessToken);
    }

    public Optional<Member> findByApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey);
    }

    public String genAccessToken(Member member) {
        return authTokenService.genAccessToken(member);
    }
}
