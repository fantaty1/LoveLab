package com.lovelab.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> domain(ApiException error, HttpServletRequest request) {
        return ResponseEntity.status(error.status).body(ApiError.of(request, error.code, error.getMessage()));
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> invalid(Exception error, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiError.of(request, "INVALID_REQUEST", "请求格式或字段不符合要求"));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(HttpServletRequest request) {
        return ResponseEntity.status(409).body(ApiError.of(request, "CONFLICT", "记录已存在或数据关联不符合要求"));
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> denied(HttpServletRequest request) {
        return ResponseEntity.status(403).body(ApiError.of(request,"FORBIDDEN","无权执行此操作"));
    }
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> missing(HttpServletRequest request) {
        return ResponseEntity.status(404).body(ApiError.of(request, "NOT_FOUND", "接口不存在"));
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> method(HttpServletRequest request) {
        return ResponseEntity.status(405).body(ApiError.of(request,"METHOD_NOT_ALLOWED","接口不支持此HTTP方法"));
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiError> media(HttpServletRequest request) {
        return ResponseEntity.status(415).body(ApiError.of(request,"UNSUPPORTED_MEDIA_TYPE","请求须使用application/json"));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception error, HttpServletRequest request) {
        log.error("Request failed requestId={} type={}", request.getAttribute("requestId"), error.getClass().getSimpleName());
        return ResponseEntity.internalServerError().body(ApiError.of(request, "INTERNAL_ERROR", "服务暂时不可用，请稍后重试"));
    }
}
