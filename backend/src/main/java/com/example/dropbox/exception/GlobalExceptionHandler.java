package com.example.dropbox.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<String> handleAuthException(AuthException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(SizeLimtException.class)
    public ResponseEntity<String> handleFileSizeLimit(SizeLimtException e) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneral(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
    }

    @ExceptionHandler({ ErrorResponseException.class, InsufficientDataException.class,
            InternalException.class, InvalidResponseException.class,
            ServerException.class, XmlParserException.class })
    public ResponseEntity<String> handleMinioException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body("Storage service error: " + e.getMessage());
    }
}
