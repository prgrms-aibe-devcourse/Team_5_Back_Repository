package com.team_5_back_repository.project.domain.member.controller;

import com.team_5_back_repository.project.domain.member.service.MemberService;
import com.team_5_back_repository.project.global.email.service.EmailService;
import com.team_5_back_repository.project.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@Tag(name = "Email Verify Controller", description = "이메일 인증 기능 제공")
public class EmailController {

    private final MemberService memberService;
    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(summary = "이메일 전송", description = "현재 사용하려는 이메일이 사용가능한지 확인한 후, 인증 번호를 해당 이메일로 전송합니다.")
    public RsData<Boolean> sendEmail(@RequestParam String email) throws MessagingException {
        if(!memberService.isEmailAvailable(email)) {
            return new RsData<>("409", "이미 사용 중인 이메일입니다.", false);
        }
        emailService.sendEmail(email);
        return new RsData<>("200", "이메일 전송 성공", true);
    }

    @PostMapping("/verify")
    @Operation(summary = "이메일 인증", description = "현재 사용하려는 이메일 인증번호를 Redis에 저장된 인증 번호와 비교하여 인증을 완료합니다.")
    public RsData<Boolean> verifyEmail(@RequestParam String email, @RequestParam Integer verificationCode) {
        boolean isVerified = emailService.verifyCode(email, verificationCode);
        if(isVerified) return new RsData<>("200", "인증 완료", true);
        else return new RsData<>("200", "인증 실패", false);
    }
}
