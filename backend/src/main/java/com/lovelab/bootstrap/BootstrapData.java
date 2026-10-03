package com.lovelab.bootstrap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovelab.auth.AccountRepository;
import com.lovelab.auth.AccountService;
import com.lovelab.content.ContentKind;
import com.lovelab.content.ContentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.UUID;

@Component
public class BootstrapData implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BootstrapData.class);
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final ContentValidator validator;
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;
    @Value("${lovelab.seed-enabled}") private boolean seed;
    @Value("${lovelab.bootstrap.username}") private String username;
    @Value("${lovelab.bootstrap.password}") private String password;
    public BootstrapData(JdbcTemplate jdbc, ObjectMapper mapper, ContentValidator validator, AccountRepository accounts, PasswordEncoder encoder) {
        this.jdbc = jdbc; this.mapper = mapper; this.validator = validator; this.accounts = accounts; this.encoder = encoder;
    }
    @Override @Transactional public void run(ApplicationArguments args) throws Exception {
        if (!username.isBlank()) {
            String normalized = AccountService.normalize(username);
            if (!normalized.matches("[a-z0-9_]{3,32}")) throw new IllegalStateException("Invalid bootstrap username");
            if (accounts.find(normalized).isEmpty()) {
                AccountService.validatePassword(password); accounts.insert(normalized, encoder.encode(password), "ADMIN");
                log.info("Bootstrap administrator created; remove ADMIN_PASSWORD from the runtime environment afterwards");
            }
        }
        if (!seed) return;
        JsonNode root;
        try (var stream = new ClassPathResource("content/seed.json").getInputStream()) { root = mapper.readTree(stream); }
        if (root.path("formatVersion").asInt() != 1) throw new IllegalStateException("Unsupported seed format");
        for (var group : Map.of("stories", ContentKind.STORY, "cards", ContentKind.CARD, "tutorials", ContentKind.TUTORIAL, "questions", ContentKind.QUESTION).entrySet()) {
            for (JsonNode document : root.path(group.getKey())) {
                String slug = document.path(group.getValue() == ContentKind.TUTORIAL ? "slug" : "id").asText();
                validator.validate(group.getValue(), slug, document);
                if (jdbc.queryForObject("SELECT COUNT(*) FROM content_entries WHERE kind=? AND slug=?", Integer.class, group.getValue().name(), slug) > 0) continue;
                UUID id = UUID.randomUUID();
                jdbc.update("INSERT INTO content_entries(id,kind,slug) VALUES (?,?,?)", id, group.getValue().name(), slug);
                jdbc.update("INSERT INTO content_versions(content_id,version,document,status,published_at) VALUES (?,1,?,'PUBLISHED',CURRENT_TIMESTAMP)", id, document.toString());
                jdbc.update("UPDATE content_entries SET published_version=1 WHERE id=?", id);
            }
        }
        if (mapper.readTree(jdbc.queryForObject("SELECT document FROM content_metadata WHERE id='handbook'",String.class)).path("topics").isEmpty()) jdbc.update("UPDATE content_metadata SET document=? WHERE id='handbook'", root.path("metadata").toString());
        log.info("Example content available; existing versions were preserved");
    }
}
