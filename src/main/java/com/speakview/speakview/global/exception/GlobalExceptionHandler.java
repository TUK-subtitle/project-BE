package com.speakview.speakview.global.exception;

import com.speakview.speakview.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 정의한 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException e) {

        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(
                        errorCode.getStatus().value(),
                        errorCode.getMessage(),
                        e.getDetail()
                ));
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {

        return ResponseEntity
                .status(500)
                .body(ApiResponse.fail(
                        500,
                        "서버 내부 오류가 발생했습니다.",
                        null
                ));
    }
}