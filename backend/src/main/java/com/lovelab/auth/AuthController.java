package com.lovelab.auth;

import com.lovelab.common.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    public record Credentials(@NotBlank @Pattern(regexp="[A-Za-z0-9_]{3,32}") String username,
                              @NotBlank @Size(min=12, max=72) String password) {}
    private final AccountService accounts;
    private final AuthenticationManager manager;
    private final HttpSessionSecurityContextRepository contexts;
    private final HttpSessionCsrfTokenRepository csrf;
    public AuthController(AccountService accounts, AuthenticationManager manager, HttpSessionSecurityContextRepository contexts, HttpSessionCsrfTokenRepository csrf) {
        this.accounts = accounts; this.manager = manager; this.contexts = contexts; this.csrf = csrf;
    }
    @GetMapping("/csrf") Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken(), "headerName", token.getHeaderName()); }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    Account.UserView register(@Valid @RequestBody Credentials request) { return accounts.register(request.username, request.password).view(); }
    @PostMapping("/login") Account.UserView login(@Valid @RequestBody Credentials body, HttpServletRequest request, HttpServletResponse response) {
        try {
            var authentication = manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(AccountService.normalize(body.username), body.password));
            new ChangeSessionIdAuthenticationStrategy().onAuthentication(authentication, request, response);
            var context = SecurityContextHolder.createEmptyContext(); context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context); contexts.saveContext(context, request, response);
            csrf.saveToken(null, request, response);
            return ((Account) authentication.getPrincipal()).view();
        } catch (AuthenticationException error) { throw new ApiException(401, "INVALID_CREDENTIALS", "用户名或密码不正确"); }
    }
    @GetMapping("/me") Account.UserView me(@AuthenticationPrincipal Account account) { return account.view(); }
}
