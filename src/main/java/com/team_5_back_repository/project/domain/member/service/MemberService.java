package com.team_5_back_repository.project.domain.member.service;

import com.team_5_back_repository.project.domain.member.dto.*;
import com.team_5_back_repository.project.domain.member.entity.ActivityRegion;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.entity.Region;
import com.team_5_back_repository.project.domain.member.exception.MemberException;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import com.team_5_back_repository.project.domain.member.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final AuthTokenService authTokenService;
    private final MemberRepository memberRepository;
    private final RegionService regionService;
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
        Member member = memberJoinRequest.toEntity();
        regionService.saveRegions(memberJoinRequest.getRegions());

        for (RegionDto regionDto : memberJoinRequest.getRegions()) {
            Region region = regionService.findById(
                    Long.parseLong(regionDto.getCode()))
                    .orElseThrow(() -> new MemberException("404-1", "존재하지 않는 지역입니다."));

            ActivityRegion activityRegion = ActivityRegion.builder()
                    .region(region)
                    .member(member)
                    .build();

            member.addActivityRegion(activityRegion);
        }
        Member savedMember = memberRepository.save(member);
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

    @Transactional
    public Member joinOrModify(String username, String password, String nickname) {
        Member member = memberRepository.findByEmail(username).orElse(null);
        if (member == null) {
            Member newMember = Member.builder()
                    .email(username)
                    .password(password)
                    .nickname(nickname)
                    .apiKey(UUID.randomUUID().toString())
                    .build();
            return memberRepository.save(newMember);
        } else {
            return modifyMember(member, nickname, null);
        }
    }

    @Transactional
    public Member modifyMember(Member member, String nickname, String introduction) {
        if(introduction != null)
            member.setIntroduction(introduction);
        if(nickname != null)
            member.setNickname(nickname);
        return memberRepository.save(member);
    }

    public Optional<Member> findById(long id) {
        return memberRepository.findById(id);
    }

}
