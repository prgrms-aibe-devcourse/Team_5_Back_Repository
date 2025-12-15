package com.team_5_back_repository.project.global.globalExceptionHandler;

import com.team_5_back_repository.project.domain.member.exception.MemberException;
import com.team_5_back_repository.project.global.redis.UnAuthenticationException;
import com.team_5_back_repository.project.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData> handle(NoSuchElementException e) {
        return new ResponseEntity<>(
                new RsData(
                        "404-001",
                        "존재하지 않는 데이터에 접근했습니다.",
                        null
                ),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(MemberException.class)
    public RsData<Void> handle(MemberException e, HttpServletResponse response) {
        RsData<Void>  rsData = e.getRsData();
        response.setStatus(rsData.statusCode());
        return rsData;
    }

    @ExceptionHandler(UnAuthenticationException.class)
    public RsData<Void> handle(UnAuthenticationException e, HttpServletResponse response) {
        RsData<Void> rsData = e.getRsData();
        response.setStatus(rsData.statusCode());
        return rsData;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RsData<Void>> handle(IllegalArgumentException e) {
        return new ResponseEntity<>(
                new RsData<>("400-001", e.getMessage(), null),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RsData<Void>> handle(IllegalStateException e) {
        return new ResponseEntity<>(
                new RsData<>("403-002", e.getMessage(), null),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<RsData<Void>> handle(SecurityException e) {
        return new ResponseEntity<>(
                new RsData<>("403-001", "권한이 없습니다.", null),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("잘못된 요청입니다.");
        return new ResponseEntity<>(
                new RsData<>("400-002", msg, null),
                HttpStatus.BAD_REQUEST
        );
    }
}