package com.portfolio.backend.global.error;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {
    
    private boolean success;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private Map<String, String> errors;  // 필드별 에러 (validation)
    
    public static ErrorResponse of(String message, int status) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(String message, int status, Map<String, String> errors) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }
}
