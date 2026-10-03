package com.lovelab.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountRepository {
    private final JdbcTemplate jdbc;
    public AccountRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public Optional<Account> find(String username) {
        return jdbc.query("SELECT id, username, password_hash, role FROM app_users WHERE username = ?",
                (rs, row) -> new Account(rs.getObject("id", UUID.class), rs.getString("username"), rs.getString("password_hash"), rs.getString("role")), username).stream().findFirst();
    }
    public Account insert(String username, String hash, String role) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO app_users(id, username, password_hash, role) VALUES (?, ?, ?, ?)", id, username, hash, role);
        return new Account(id, username, hash, role);
    }
}
