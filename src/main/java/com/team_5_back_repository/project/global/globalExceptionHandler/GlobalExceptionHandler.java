package com.team_5_back_repository.project.global.globalExceptionHandler;

import com.team_5_back_repository.project.domain.member.exception.MemberException;
import com.team_5_back_repository.project.global.redis.UnAuthenticationException;
import com.team_5_back_repository.project.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
                        "존재하지 않는 데이터에 접근했습니다."
                        , null
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
        RsData<Void>  rsData = e.getRsData();
        response.setStatus(rsData.statusCode());
        return rsData;
    }
}
