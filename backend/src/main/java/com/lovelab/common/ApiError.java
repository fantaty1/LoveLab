package com.lovelab.common;

import jakarta.servlet.http.HttpServletRequest;

public record ApiError(String code, String message, String requestId) {
    public static ApiError of(HttpServletRequest request, String code, String message) {
        return new ApiError(code, message, (String) request.getAttribute("requestId"));
    }
}
