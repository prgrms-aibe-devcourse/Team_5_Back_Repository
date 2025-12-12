package com.team_5_back_repository.project.domain.member.controller;

import com.team_5_back_repository.project.domain.member.dto.dto.MemberDto;
import com.team_5_back_repository.project.domain.member.dto.request.ChangePasswordRequest;
import com.team_5_back_repository.project.domain.member.dto.request.MemberJoinRequest;
import com.team_5_back_repository.project.domain.member.dto.request.MemberLoginRequest;
import com.team_5_back_repository.project.domain.member.dto.request.WithDrawRequest;
import com.team_5_back_repository.project.domain.member.dto.response.MemberLoginResponse;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.exception.MemberException;
import com.team_5_back_repository.project.domain.member.service.MemberService;
import com.team_5_back_repository.project.global.email.service.EmailService;
import com.team_5_back_repository.project.global.rsData.RsData;
import com.team_5_back_repository.project.global.security.Rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "인증 및 회원정보 조회 기능 제공")
public class MemberController {
    private final MemberService memberService;
    private final Rq rq;
    private final EmailService emailService;

    private Member getMember() {
        Member actor = rq.getActor();
        if(actor == null){
            throw new MemberException("401-1", "로그인 후 이용해주세요.");
        }
        return actor;
    }

    @Transactional
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public RsData<MemberDto> register(@Valid @RequestBody MemberJoinRequest memberJoinRequest) {
        MemberDto memberDto = memberService.join(memberJoinRequest);
        return new RsData<>("201-1", "회원가입 성공", memberDto);
    }

    @Transactional(readOnly = true)
    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인", description = "닉네임이 이미 사용 중인지 확인합니다.")
    public RsData<Boolean> checkNickname(@RequestParam @Size(min = 2, max = 10)String nickname) {
        boolean isAvailable = memberService.isNicknameAvailable(nickname);
        if (isAvailable) {
            return new RsData<>("200-1", "사용 가능한 닉네임입니다.", true);
        }
        return new RsData<>("409-1", "이미 사용 중인 닉네임입니다.", false);
    }

    @Transactional
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "기존 사용자가 시스템에 로그인합니다.")
    public RsData<MemberLoginResponse> login(@Valid @RequestBody MemberLoginRequest memberLoginRequest) {
        Member member = memberService.login(memberLoginRequest);
        String accessToken = memberService.genAccessToken(member);

        rq.setHeader("Authorization", "Bearer " + member.getApiKey() + " " + accessToken);
        rq.setCookie("apiKey", member.getApiKey());
        rq.setCookie("accessToken", accessToken);

        return new RsData<>("202-1", "로그인 성공",
                MemberLoginResponse.builder()
                        .id(member.getId())
                        .email(member.getEmail())
                        .nickname(member.getNickname())
                        .accessToken(accessToken)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    public RsData<MemberDto> me() {
        Member actor = getMember();
        MemberDto memberDto = memberService.findByEmail(actor.getEmail())
                .map(MemberDto::new)
                .orElseThrow(() -> new MemberException("404-1", "내 정보를 찾을 수 없습니다."));
        return new RsData<>("203-1", "내 정보 조회 성공", memberDto);
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자를 로그아웃 처리합니다.")
    public RsData<Void> logout() {
        rq.deleteCookie("apiKey");
        rq.deleteCookie("accessToken");
        return new RsData<>("204-1", "로그아웃 성공", null);
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 회원 탈퇴를 처리합니다.")
    public RsData<Void> withdraw(@RequestBody WithDrawRequest request) {
        Member actor = getMember();
        memberService.withdraw(actor.getId(), request.getPassword());
        return new RsData<>("205-1", "회원탈퇴 성공", null);
    }

    @PostMapping("/change-password")
    @Operation(summary = "비밀번호 변경", description = "현재 로그인한 사용자의 비밀번호를 변경합니다.")
    public RsData<Void> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        Member actor = getMember();
        if(!emailService.verifyCode(actor.getEmail(), changePasswordRequest.getAuthCode())){
            return new RsData<>("401-2", "인증 코드가 올바르지 않습니다.", null);
        }
        memberService.changePassword(actor.getId(), changePasswordRequest);
        return new RsData<>("206-1", "비밀번호 변경 성공", null);
    }
}
