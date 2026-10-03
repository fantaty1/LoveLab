package com.lovelab.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovelab.auth.AccountService;
import com.lovelab.auth.AccountRepository;
import com.lovelab.auth.AccountRefreshFilter;
import com.lovelab.auth.AuthRateLimitFilter;
import com.lovelab.common.ApiError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean HttpSessionSecurityContextRepository contextRepository() { return new HttpSessionSecurityContextRepository(); }
    @Bean HttpSessionCsrfTokenRepository csrfRepository() { return new HttpSessionCsrfTokenRepository(); }
    @Bean AuthenticationManager authenticationManager(AccountService accounts, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(accounts);
        provider.setPasswordEncoder(encoder); return new ProviderManager(provider);
    }
    @Bean SecurityFilterChain security(HttpSecurity http, ObjectMapper mapper, AccountRepository accounts, HttpSessionSecurityContextRepository contexts,
                                      HttpSessionCsrfTokenRepository csrf, @Value("${lovelab.allowed-origins}") String origins) throws Exception {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList());
        cors.setAllowCredentials(true); cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN")); cors.setExposedHeaders(List.of("X-Request-Id"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/api/**", cors);
        http.cors(c -> c.configurationSource(source)).csrf(c -> c.csrfTokenRepository(csrf))
            .securityContext(c -> c.securityContextRepository(contexts))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.GET, "/actuator/health", "/api/v1/auth/csrf", "/api/v1/content/**", "/api/v1/daily").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/feedback").permitAll()
                .requestMatchers("/api/v1/admin/**").hasAnyRole("EDITOR", "ADMIN")
                .requestMatchers("/api/**").authenticated().anyRequest().denyAll())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> { res.setStatus(401); res.setContentType("application/json;charset=UTF-8"); mapper.writeValue(res.getOutputStream(), ApiError.of(req, "UNAUTHENTICATED", "请先登录")); })
                .accessDeniedHandler((req, res, ex) -> { res.setStatus(403); res.setContentType("application/json;charset=UTF-8"); mapper.writeValue(res.getOutputStream(), ApiError.of(req, "FORBIDDEN", "无权访问或安全令牌已失效")); }))
            .logout(l -> l.logoutUrl("/api/v1/auth/logout").deleteCookies("LOVELAB_SESSION").logoutSuccessHandler((req, res, auth) -> res.setStatus(204)))
            .addFilterBefore(new AuthRateLimitFilter(mapper), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new AccountRefreshFilter(accounts), AuthorizationFilter.class);
        return http.build();
    }
}
