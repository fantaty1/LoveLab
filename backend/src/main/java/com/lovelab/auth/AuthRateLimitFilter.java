package com.lovelab.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovelab.common.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthRateLimitFilter extends OncePerRequestFilter {
    private record Bucket(long expires, int count) {}
    private final Map<String, Bucket> buckets = new HashMap<>();
    private final ObjectMapper mapper;
    public AuthRateLimitFilter(ObjectMapper mapper) { this.mapper = mapper; }
    private synchronized boolean allow(String key, int maximum) {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> entry.getValue().expires < now);
        Bucket bucket = buckets.get(key);
        if (bucket == null && buckets.size() >= 10000) return false;
        if (bucket == null) bucket = new Bucket(now + 900_000, 0);
        if (bucket.count >= maximum) return false;
        buckets.put(key, new Bucket(bucket.expires, bucket.count + 1)); return true;
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (request.getMethod().equals("POST") && (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register") || path.equals("/api/v1/feedback"))) {
            int maximum = path.endsWith("register") ? 5 : path.endsWith("feedback") ? 30 : 20;
            if (!allow(request.getRemoteAddr() + ":" + path, maximum)) {
                response.setStatus(429); response.setHeader("Retry-After", "900"); response.setContentType("application/json;charset=UTF-8");
                mapper.writeValue(response.getOutputStream(), ApiError.of(request, "RATE_LIMITED", "尝试过于频繁，请稍后再试")); return;
            }
        }
        chain.doFilter(request, response);
    }
}
