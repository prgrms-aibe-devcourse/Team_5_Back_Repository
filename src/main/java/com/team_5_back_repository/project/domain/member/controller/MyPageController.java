package com.team_5_back_repository.project.domain.member.controller;

import com.team_5_back_repository.project.domain.member.dto.dto.MemberDto;
import com.team_5_back_repository.project.domain.member.dto.dto.MemberEditDto;
import com.team_5_back_repository.project.domain.member.dto.dto.MyPageDto;
import com.team_5_back_repository.project.domain.member.dto.request.MemberEditRequest;
import com.team_5_back_repository.project.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "MyPage Controller", description = "회원이 자신의 정보 를 조회 및 수정하는 기능 제공")
public class MyPageController {

    private final MemberService memberService;

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    @Operation(summary = "마이페이지 조회", description = "회원이 자신의 정보를 조회합니다.")
    public MyPageDto getMyPage(@PathVariable Long id) {
        return memberService.retrieveMemberById(id);
    }

    @Transactional(readOnly = true)
    @GetMapping("/edit/{id}")
    @Operation(summary = "회원 정보 수정 페이지 조회", description = "회원이 자신의 정보를 수정하기 위해 현재 정보를 조회합니다.")
    public MemberEditDto getMyPageForEdit(@PathVariable Long id) {
        return memberService.retrieveModifyMemberById(id);
    }

    @Transactional
    @PutMapping("/{id}")
    @Operation(summary = "마이페이지 수정", description = "회원이 자신의 정보를 수정합니다.")
    public MemberDto editMyPage(@PathVariable Long id,@Valid @RequestBody MemberEditRequest memberEditRequest) {
        return memberService.modifyMember(id, memberEditRequest);
    }
}
