package com.lovelab;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovelab.auth.AuthRateLimitFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitTest {
    @Test void loginLimitIsSeparatedBySourceAndReturnsRetryAfter() throws Exception {
        var filter=new AuthRateLimitFilter(new ObjectMapper());
        var accepted=new AtomicInteger();
        for (int i=0;i<21;i++) {
            var request=new MockHttpServletRequest("POST","/api/v1/auth/login"); request.setRemoteAddr("192.0.2.10");
            var response=new MockHttpServletResponse();
            filter.doFilter(request,response,(req,res)->accepted.incrementAndGet());
            if (i==20) { assertThat(response.getStatus()).isEqualTo(429); assertThat(response.getHeader("Retry-After")).isEqualTo("900"); }
        }
        var other=new MockHttpServletRequest("POST","/api/v1/auth/login"); other.setRemoteAddr("192.0.2.11");
        filter.doFilter(other,new MockHttpServletResponse(),(req,res)->accepted.incrementAndGet());
        assertThat(accepted.get()).isEqualTo(21);
    }
}
