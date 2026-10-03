package com.lovelab.auth;

import com.lovelab.common.ApiException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AccountService implements UserDetailsService {
    private final AccountRepository repository;
    private final PasswordEncoder encoder;
    public AccountService(AccountRepository repository, PasswordEncoder encoder) { this.repository = repository; this.encoder = encoder; }
    public static String normalize(String username) { return username.trim().toLowerCase(Locale.ROOT); }
    public static void validatePassword(String password) {
        if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) throw ApiException.bad("密码至少12个字符，UTF-8编码不超过72字节");
    }
    @Override public Account loadUserByUsername(String username) {
        return repository.find(normalize(username)).orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }
    @Transactional public Account register(String username, String password) {
        validatePassword(password);
        return repository.insert(normalize(username), encoder.encode(password), "USER");
    }
}
