package com.team_5_back_repository.project.domain.member.service;

import com.team_5_back_repository.project.domain.member.dto.dto.*;
import com.team_5_back_repository.project.domain.member.dto.request.MemberEditRequest;
import com.team_5_back_repository.project.domain.member.dto.request.MemberJoinRequest;
import com.team_5_back_repository.project.domain.member.dto.request.MemberLoginRequest;
import com.team_5_back_repository.project.domain.member.entity.ActivityRegion;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.entity.Region;
import com.team_5_back_repository.project.domain.member.exception.MemberException;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import com.team_5_back_repository.project.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final AuthTokenService authTokenService;
    private final MemberRepository memberRepository;
    private final RegionService regionService;
    private final PasswordEncoder passwordEncoder;
    private final PostService postService;

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

    public boolean isEmailAvailable(String email) {
        return memberRepository.findByEmail(email).isEmpty();
    }

    public boolean isNicknameAvailable(String nickname) {
        return memberRepository.findByNickname(nickname).isEmpty();
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

    @Transactional
    public MemberDto modifyMember(Long id, MemberEditRequest memberEditRequest) {

        Member member = memberRepository.findMemberWithRegions(id)
                .orElseThrow(() -> new MemberException("404-1", "존재하지 않는 회원입니다."));

        regionService.saveRegions(memberEditRequest.getRegions());

        member.setNickname(memberEditRequest.getNickname());
        member.setIntroduction(memberEditRequest.getIntroduction());
        member.setEmail(memberEditRequest.getEmail());

        List<ActivityRegion> oldRegions = member.getActivityRegions();

        List<Long> newRegionIds = memberEditRequest.getRegions().stream()
                .map(r -> Long.parseLong(r.getCode()))
                .toList();


        List<ActivityRegion> toRemove = oldRegions.stream()
                .filter(ar -> !newRegionIds.contains(ar.getRegion().getId()))
                .toList();

        member.getActivityRegions().removeAll(toRemove);


        for (Long regionId : newRegionIds) {

            boolean exists = oldRegions.stream()
                    .anyMatch(ar -> ar.getRegion().getId().equals(regionId));

            if (!exists) {
                Region region = regionService.findById(regionId)
                        .orElseThrow(() -> new MemberException("404-1", "존재하지 않는 지역입니다."));

                ActivityRegion activityRegion = ActivityRegion.builder()
                        .region(region)
                        .member(member)
                        .build();

                member.addActivityRegion(activityRegion);
            }
        }

        return new MemberDto(memberRepository.save(member));
    }

    public Optional<Member> findById(long id) {
        return memberRepository.findMemberWithRegions(id);
    }

    public MyPageDto retrieveMemberById(long id) {
        Member member = memberRepository.findMemberWithRegions(id)
                .orElseThrow(() -> new MemberException("404-1", "존재하지 않는 회원입니다."));

        List<String> activityRegions = member.getActivityRegions().stream()
                .map(ar -> ar.getRegion().getFullName())
                .toList();

        Long postCount = postService.countPostsByMember(member);
        Long commentCount = 0L; // TODO 댓글, 좋아요, 팔로워, 팔로잉 추후 구현 필요
        Long likeCount = 0L; // 추후 구현 필요
        Long followerCount = 0L; // 추후 구현 필요
        Long followingCount = 0L; // 추후 구현 필요

        return MyPageDto.builder()
                .nickname(member.getNickname())
                .regions(activityRegions)
                .introduction(member.getIntroduction())
                .joinDate(member.getCreatedAt().toString())
                .stats(new MemberStatDto(postCount, commentCount, likeCount, followerCount, followingCount))
                .build();
    }

    public MemberEditDto retrieveModifyMemberById(long id) {
        Member member = memberRepository.findMemberWithRegions(id)
                .orElseThrow(() -> new MemberException("404-1", "존재하지 않는 회원입니다."));

        List<RegionDto> activityRegions = member.getActivityRegions().stream()
                .map(ar -> new RegionDto(
                        String.valueOf(ar.getRegion().getId()),
                        ar.getRegion().getFullName(),
                        ar.getRegion().getShortName()
                ))
                .collect(Collectors.toList());

        return MemberEditDto.builder()
                .nickname(member.getNickname())
                .regions(activityRegions)
                .introduction(member.getIntroduction())
                .email(member.getEmail())
                .build();
    }

}
