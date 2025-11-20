package com.team_5_back_repository.project.global.email.service;

import com.team_5_back_repository.project.global.redis.RedisService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Transactional
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    private final RedisService redisService;

    private static final String EMAIL_TITLE = "OneLife 인증 번호 안내 이메일입니다.";
    private static final String EMAIL_CONTENT_TEMPLATE = "아래의 인증번호를 입력하여 회원가입을 완료해주세요.\n"+
            "인증번호 : %d";

    public void sendEmail(String toEmail) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject(EMAIL_TITLE);
        int code = codeGenerator();
        redisService.setCode(toEmail, code);
        helper.setText(String.format(EMAIL_CONTENT_TEMPLATE, code)); // true를 설정해서 HTML을 사용 가능하게 함
        try {
            emailSender.send(message);
        } catch (RuntimeException e) {
            e.printStackTrace(); // 또는 로거를 사용하여 상세한 예외 정보 로깅
            throw new RuntimeException("Unable to send email in sendEmail", e); // 원인 예외를 포함시키기
        }
    }

    public boolean verifyCode(String email, Integer code) {
        Integer storedCode = redisService.getCode(email);
        if (storedCode != null && storedCode.equals(code)) {
            redisService.deleteCode(email); // 인증이 성공하면 코드 삭제
            return true;
        }
        return false;
    }

    public int codeGenerator() {
        Random r = new Random();
        StringBuilder randomNumber = new StringBuilder();
        for(int i = 0; i < 6; i++) {
            randomNumber.append(r.nextInt(10));
        }
        return Integer.parseInt(randomNumber.toString());
    }
}