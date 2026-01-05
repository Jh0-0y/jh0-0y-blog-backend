package com.blog.backend.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // 비즈니스 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(CustomException e) {
        log.error("BusinessException: {}", e.getMessage());
        
        ErrorResponse response = ErrorResponse.of(
                e.getMessage(),
                e.getStatus().value()
        );
        
        return ResponseEntity.status(e.getStatus()).body(response);
    }
    
    // Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("ValidationException: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse response = ErrorResponse.of(
                "입력값이 올바르지 않습니다",
                HttpStatus.BAD_REQUEST.value(),
                errors
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // 그 외 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);
        
        ErrorResponse response = ErrorResponse.of(
                "서버 오류가 발생했습니다",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.internalServerError().body(response);
    }
}
