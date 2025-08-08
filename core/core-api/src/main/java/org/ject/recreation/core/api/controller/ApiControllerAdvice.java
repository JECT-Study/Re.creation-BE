package org.ject.recreation.core.api.controller;

import org.ject.recreation.core.support.error.CoreException;
import org.ject.recreation.core.support.error.ErrorData;
import org.ject.recreation.core.support.error.ErrorType;
import org.ject.recreation.core.support.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiControllerAdvice {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ApiResponse<?>> handleCoreException(CoreException e) {
        switch (e.getErrorType().getLogLevel()) {
            case ERROR -> log.error("CoreException : {}", e.getMessage(), e);
            case WARN -> log.warn("CoreException : {}", e.getMessage(), e);
            default -> log.info("CoreException : {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(ApiResponse.error(e.getErrorType(), e.getData()), e.getErrorType().getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException : {}", e.getMessage(), e);

        ErrorData errorData = new ErrorData();

        // 1. FieldError 처리
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errorData.and(fieldError.getField(), fieldError.getRejectedValue());
        });
        // 2. ObjectError 중 FieldError가 아닌 것만 처리
        e.getBindingResult().getGlobalErrors().forEach(globalError -> {
            errorData.and("global", globalError.getDefaultMessage());
        });

        return new ResponseEntity<>(ApiResponse.error(ErrorType.VALIDATION_ERROR, errorData), ErrorType.VALIDATION_ERROR.getStatus());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("MethodArgumentTypeMismatchException: {}", e.getMessage(), e);

        ErrorData errorData = ErrorData.of(e.getName(), e.getValue());

        return new ResponseEntity<>(ApiResponse.error(ErrorType.VALIDATION_ERROR, errorData), ErrorType.VALIDATION_ERROR.getStatus());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("Exception : {}", e.getMessage(), e);
        return new ResponseEntity<>(ApiResponse.error(ErrorType.DEFAULT_ERROR), ErrorType.DEFAULT_ERROR.getStatus());
    }

}
