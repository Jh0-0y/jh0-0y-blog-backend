package com.blog.backend.global.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class CustomException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, String> errors;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errors = null;
    }

    public CustomException(String message, HttpStatus status, Map<String, String> errors) {
        super(message);
        this.status = status;
        this.errors = errors;
    }

    // ========== 일반 에러 ========== //

    public static CustomException notFound(String message) {
        return new CustomException(message, HttpStatus.NOT_FOUND);
    }

    public static CustomException badRequest(String message) {
        return new CustomException(message, HttpStatus.BAD_REQUEST);
    }

    public static CustomException unauthorized(String message) {
        return new CustomException(message, HttpStatus.UNAUTHORIZED);
    }

    public static CustomException forbidden(String message) {
        return new CustomException(message, HttpStatus.FORBIDDEN);
    }

    public static CustomException conflict(String message) {
        return new CustomException(message, HttpStatus.CONFLICT);
    }

    // ========== 필드 에러 (UI 필드 표시용) ========== //

    /**
     * 단일 필드 에러
     * 예: CustomException.fieldError("title", "이미 사용 중인 제목입니다")
     */
    public static CustomException fieldError(String field, String message) {
        return new CustomException(
                "입력값이 올바르지 않습니다",
                HttpStatus.BAD_REQUEST,
                Map.of(field, message)
        );
    }

    /**
     * 다중 필드 에러
     * 예: CustomException.fieldErrors(Map.of("title", "제목 오류", "content", "본문 오류"))
     */
    public static CustomException fieldErrors(Map<String, String> errors) {
        return new CustomException(
                "입력값이 올바르지 않습니다",
                HttpStatus.BAD_REQUEST,
                errors
        );
    }

    /**
     * errors 필드 존재 여부 확인
     */
    public boolean hasFieldErrors() {
        return errors != null && !errors.isEmpty();
    }
}