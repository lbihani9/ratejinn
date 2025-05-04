package com.jinn.ratejinn.controller;

import com.jinn.ratejinn.common.enums.RateJinnErrorCode;
import com.jinn.ratejinn.dto.RateLimitErrorResponse;
import com.jinn.ratejinn.exception.RateLimitServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitServiceException.class)
    public ResponseEntity<RateLimitErrorResponse> handleServiceError(RateLimitServiceException ex) {
        RateLimitErrorResponse errorResponse = new RateLimitErrorResponse(ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RateLimitErrorResponse> handleGenericError(Exception ex) {
        RateLimitErrorResponse errorResponse = new RateLimitErrorResponse(RateJinnErrorCode.INTERNAL_ERROR, "Something went wrong.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

