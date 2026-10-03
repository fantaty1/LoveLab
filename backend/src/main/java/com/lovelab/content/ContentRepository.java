package com.lovelab.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lovelab.common.ApiException;
import com.lovelab.common.JsonCodec;
import com.lovelab.common.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public class ContentRepository {
    private final JdbcTemplate jdbc;
    private final JsonCodec json;
    private final RowMapper<ContentSnapshot> mapping;
    private static final String SELECT = "SELECT e.id,e.kind,e.slug,v.version,v.status,v.document,v.created_by,v.reviewed_by FROM content_entries e JOIN content_versions v ON v.content_id=e.id ";
    public ContentRepository(JdbcTemplate jdbc, JsonCodec json) {
        this.jdbc = jdbc; this.json = json;
        mapping = (rs, row) -> new ContentSnapshot(rs.getObject("id", UUID.class), ContentKind.valueOf(rs.getString("kind")), rs.getString("slug"), rs.getInt("version"), rs.getString("status"), json.read(rs.getString("document")), rs.getObject("created_by", UUID.class), rs.getObject("reviewed_by", UUID.class));
    }
    public ContentSnapshot published(ContentKind kind, String slug) {
        return jdbc.query(SELECT + "WHERE e.kind=? AND e.slug=? AND v.version=e.published_version", mapping, kind.name(), slug).stream().findFirst().orElseThrow(ApiException::missing);
    }
    public ContentSnapshot version(UUID id, int version) {
        return jdbc.query(SELECT + "WHERE e.id=? AND v.version=?", mapping, id, version).stream().findFirst().orElseThrow(ApiException::missing);
    }
    public UUID entry(ContentKind kind, String slug, boolean lock) {
        return jdbc.query("SELECT id FROM content_entries WHERE kind=? AND slug=?" + (lock ? " FOR UPDATE" : ""), (rs, row) -> rs.getObject(1, UUID.class), kind.name(), slug).stream().findFirst().orElseThrow(ApiException::missing);
    }
    public boolean isPublished(UUID id) { return Boolean.TRUE.equals(jdbc.queryForObject("SELECT published_version IS NOT NULL FROM content_entries WHERE id=?", Boolean.class, id)); }
    public PageResult<ContentSnapshot.ContentView> list(ContentKind kind, String q, String topic, int page, int size) {
        PageResult.validate(page, size);
        String needle = q.toLowerCase().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        String conditions = "WHERE e.kind=? AND v.version=e.published_version AND LOWER(v.document) LIKE ? ESCAPE '!' ";
        var matches = jdbc.query(SELECT + conditions + "ORDER BY e.slug", mapping, kind.name(), "%" + needle + "%").stream()
                .filter(c -> topic.isBlank() || c.document().path("topic").asText().equals(topic) || c.document().path("category").asText().equals(topic)).toList();
        List<ContentSnapshot.ContentView> items = matches.stream().skip((long)page * size).limit(size).map(c -> {
            ObjectNode summary = ((ObjectNode)c.document()).deepCopy();
            summary.remove(List.of("scenes", "sections", "example", "pitfalls", "practice", "choices", "action", "caution"));
            return new ContentSnapshot.ContentView(c.kind(), c.slug(), c.version(), summary);
        }).toList();
        return new PageResult<>(items, page, size, matches.size());
    }
    public JsonNode metadata() { return json.read(jdbc.queryForObject("SELECT document FROM content_metadata WHERE id='handbook'", String.class)); }
    public void audit(UUID actor, String action, String target, String detail) {
        jdbc.update("INSERT INTO admin_audit(id,actor_id,action,target,detail) VALUES (?,?,?,?,?)", UUID.randomUUID(), actor, action, target, detail);
    }
}
