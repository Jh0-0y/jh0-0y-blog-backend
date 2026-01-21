package com.blog.backend.infra.s3.exception;

import com.blog.backend.global.core.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
public class S3ExceptionHandler {

    /**
     * S3 관련 비즈니스 예외 처리 (파일 형식 오류, 업로드 실패 등)
     */
    @ExceptionHandler(S3CustomException.class)
    public ResponseEntity<ErrorResponse> handleS3CustomException(S3CustomException e) {
        log.error("[S3_ERROR] 메시지: {}, 상태코드: {}", e.getMessage(), e.getStatus());

        ErrorResponse response = ErrorResponse.of(
                e.getMessage(),
                e.getStatus().value()
        );

        return ResponseEntity.status(e.getStatus()).body(response);
    }
}