package com.lovelab.game;

import com.lovelab.common.ApiException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class RunRepository {
    public record Run(UUID id, UUID userId, UUID contentId, int version, String currentNode, String status, int revision, OffsetDateTime updatedAt) {}
    public record Selected(int sequence, String nodeId, int optionIndex) {}
    private final JdbcTemplate jdbc;
    public RunRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public void lockUser(UUID user) { jdbc.queryForObject("SELECT id FROM app_users WHERE id=? FOR UPDATE",UUID.class,user); }
    public Run owned(UUID id, UUID user, boolean lock) {
        return jdbc.query("SELECT * FROM story_runs WHERE id=? AND user_id=?" + (lock ? " FOR UPDATE" : ""),
            (rs,row) -> new Run(rs.getObject("id",UUID.class), rs.getObject("user_id",UUID.class), rs.getObject("content_id",UUID.class), rs.getInt("version"), rs.getString("current_node"), rs.getString("status"), rs.getInt("revision"), rs.getObject("updated_at",OffsetDateTime.class)), id,user).stream().findFirst().orElseThrow(ApiException::missing);
    }
    public boolean exists(UUID id) { return jdbc.queryForObject("SELECT COUNT(*) FROM story_runs WHERE id=?", Integer.class,id) > 0; }
    public void create(UUID id, UUID user, UUID content, int version, String first) {
        jdbc.update("INSERT INTO story_runs(id,user_id,content_id,version,current_node,status) VALUES (?,?,?,?,?,'ACTIVE')",id,user,content,version,first);
    }
    public List<Selected> choices(UUID id) {
        return jdbc.query("SELECT sequence,node_id,option_index FROM run_choices WHERE run_id=? ORDER BY sequence", (rs,row)->new Selected(rs.getInt(1),rs.getString(2),rs.getInt(3)),id);
    }
    public void choose(UUID id, Selected choice, String next) {
        jdbc.update("INSERT INTO run_choices(run_id,sequence,node_id,option_index) VALUES (?,?,?,?)",id,choice.sequence,choice.nodeId,choice.optionIndex);
        jdbc.update("UPDATE story_runs SET revision=?,current_node=?,status=?,updated_at=CURRENT_TIMESTAMP,completed_at=CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE NULL END WHERE id=?",
                choice.sequence,next,next==null?"COMPLETE":"ACTIVE",next==null,id);
    }
    public List<UUID> list(UUID user, int page, int size) { return jdbc.query("SELECT id FROM story_runs WHERE user_id=? ORDER BY updated_at DESC,id LIMIT ? OFFSET ?", (rs,row)->rs.getObject(1,UUID.class),user,size,page*size); }
    public long count(UUID user) { return jdbc.queryForObject("SELECT COUNT(*) FROM story_runs WHERE user_id=?",Long.class,user); }
}
