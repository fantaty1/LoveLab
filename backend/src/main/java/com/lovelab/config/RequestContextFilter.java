package com.lovelab.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovelab.common.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component("apiRequestContextFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestContextFilter extends OncePerRequestFilter {
    private final ObjectMapper mapper;
    public RequestContextFilter(ObjectMapper mapper) { this.mapper = mapper; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String id = UUID.randomUUID().toString();
        request.setAttribute("requestId", id);
        response.setHeader("X-Request-Id", id);
        if (request.getContentLengthLong() > 1_048_576) {
            response.setStatus(413); response.setContentType("application/json;charset=UTF-8");
            mapper.writeValue(response.getOutputStream(), ApiError.of(request, "PAYLOAD_TOO_LARGE", "请求不能超过1MB")); return;
        }
        chain.doFilter(request, response);
    }
}
