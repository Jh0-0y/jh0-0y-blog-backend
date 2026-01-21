package com.blog.backend.global.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    
    private final HttpStatus status;
    
    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    
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
}
