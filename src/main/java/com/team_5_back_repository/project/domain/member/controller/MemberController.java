package com.team_5_back_repository.project.domain.member.controller;

import com.team_5_back_repository.project.domain.member.dto.MemberDto;
import com.team_5_back_repository.project.domain.member.dto.MemberJoinRequest;
import com.team_5_back_repository.project.domain.member.dto.MemberLoginResponse;
import com.team_5_back_repository.project.domain.member.service.MemberService;
import com.team_5_back_repository.project.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @Transactional
    @PostMapping("/signup")
    public RsData<MemberDto> register(@Valid @RequestBody MemberJoinRequest memberJoinRequest) {
        MemberDto memberDto = memberService.join(memberJoinRequest);
        return new RsData<>("201-1", "회원가입 성공", memberDto);
    }

    @Transactional
    @PostMapping("/login")
    public RsData<MemberLoginResponse> login(MemberDto memberDto) {
        // 로그인 로직 구현
        return new RsData<>("202-1", "로그인 성공", new MemberLoginResponse());
    }


}
