package com.lovelab.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class AccountRefreshFilter extends OncePerRequestFilter {
    private final AccountRepository accounts;
    public AccountRefreshFilter(AccountRepository accounts) { this.accounts=accounts; }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        var previous=SecurityContextHolder.getContext().getAuthentication();
        if (previous!=null && previous.getPrincipal() instanceof Account account) {
            var current=accounts.find(account.username());
            if (current.isPresent() && current.get().id().equals(account.id())) {
                var context=SecurityContextHolder.createEmptyContext();
                context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(current.get(),null,current.get().getAuthorities()));
                SecurityContextHolder.setContext(context);
            } else { SecurityContextHolder.clearContext(); if (request.getSession(false)!=null) request.getSession(false).invalidate(); }
        }
        chain.doFilter(request,response);
    }
}
