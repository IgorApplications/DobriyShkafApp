package com.iapp.iapp_messenger.config;

import com.iapp.iapp_messenger.dao.dto.ApiResponse;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальный try-catch для RestControllers
 * */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<String> handleValidation(MethodArgumentNotValidException ex) {
        String error = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return ApiResponse.error("Validation error: " + error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<String> handleIllegal(IllegalArgumentException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleAll(Exception ex) {
        return ApiResponse.error("Internal server error");
    }
}
