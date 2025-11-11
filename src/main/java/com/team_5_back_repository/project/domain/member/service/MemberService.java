package com.team_5_back_repository.project.domain.member.service;

import com.team_5_back_repository.project.domain.member.dto.MemberDto;
import com.team_5_back_repository.project.domain.member.dto.MemberJoinRequest;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.exception.MemberException;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public long countMembers() {
        return memberRepository.count();
    }

    @Transactional
    public MemberDto join(MemberJoinRequest memberJoinRequest) {
        memberRepository.findByNickname(memberJoinRequest.getNickname())
                .ifPresent(_member -> {
                    throw new MemberException("409-1", "이미 존재하는 회원입니다.");
                });

        System.out.println("asdasd" + memberJoinRequest.getPassword());
        System.out.println("asdasd" + memberJoinRequest.getNickname());
        System.out.println("asdasd" + memberJoinRequest.getEmail());
        memberJoinRequest.setPassword(passwordEncoder.encode(memberJoinRequest.getPassword()));
        Member savedMember = memberRepository.save(memberJoinRequest.toEntity());
        return new MemberDto(savedMember);
    }
}
