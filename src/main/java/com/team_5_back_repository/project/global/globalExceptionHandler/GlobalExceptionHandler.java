package com.team_5_back_repository.project.global.globalExceptionHandler;

import com.team_5_back_repository.project.global.rsData.RsData;
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
                ),
                HttpStatus.NOT_FOUND
        );
    }

    // 추가적인 예외 핸들러들을 여기에 작성할 수 있습니다.
}
