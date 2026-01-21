package com.blog.backend.infra.s3.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class S3CustomException extends RuntimeException {
    
    private final HttpStatus status;
    
    public S3CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    
    public static S3CustomException notFound(String message) {
        return new S3CustomException(message, HttpStatus.NOT_FOUND);
    }
    
    public static S3CustomException badRequest(String message) {
        return new S3CustomException(message, HttpStatus.BAD_REQUEST);
    }
    
    public static S3CustomException unauthorized(String message) {
        return new S3CustomException(message, HttpStatus.UNAUTHORIZED);
    }
    
    public static S3CustomException forbidden(String message) {
        return new S3CustomException(message, HttpStatus.FORBIDDEN);
    }
    
    public static S3CustomException conflict(String message) {
        return new S3CustomException(message, HttpStatus.CONFLICT);
    }
}
