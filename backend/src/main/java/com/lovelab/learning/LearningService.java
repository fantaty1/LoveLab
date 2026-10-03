package com.lovelab.learning;

import com.lovelab.common.ApiException;
import com.lovelab.content.ContentKind;
import com.lovelab.content.ContentRepository;
import com.lovelab.game.RunService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LearningService {
    public record Record(ContentKind kind,String slug,boolean favorite,OffsetDateTime readAt,OffsetDateTime updatedAt) {}
    public record Summary(long completedRuns,long readTutorials,long favorites,long practiceDays,List<Record> records) {}
    private final JdbcTemplate jdbc;
    private final ContentRepository content;
    private final RunService runs;
    public LearningService(JdbcTemplate jdbc,ContentRepository content,RunService runs) { this.jdbc=jdbc; this.content=content; this.runs=runs; }
    @Transactional(readOnly=true) public Summary summary(UUID user) {
        List<Record> records=jdbc.query("SELECT e.kind,e.slug,l.favorite,l.read_at,l.updated_at FROM learning_records l JOIN content_entries e ON e.id=l.content_id WHERE l.user_id=? AND (l.favorite=TRUE OR l.read_at IS NOT NULL) ORDER BY l.updated_at DESC,e.slug",
                (rs,row)->new Record(ContentKind.valueOf(rs.getString(1)),rs.getString(2),rs.getBoolean(3),rs.getObject(4,OffsetDateTime.class),rs.getObject(5,OffsetDateTime.class)),user);
        return new Summary(jdbc.queryForObject("SELECT COUNT(*) FROM story_runs WHERE user_id=? AND status='COMPLETE'",Long.class,user),
                records.stream().filter(r->r.readAt!=null).count(),records.stream().filter(Record::favorite).count(),
                jdbc.queryForObject("SELECT COUNT(DISTINCT practice_date) FROM daily_attempts WHERE user_id=?",Long.class,user),records);
    }
    private void lock(UUID user) { jdbc.queryForObject("SELECT id FROM app_users WHERE id=? FOR UPDATE",UUID.class,user); }
    @Transactional public void favorite(UUID user,ContentKind kind,String slug,boolean value) { update(user,kind,slug,value,null); }
    @Transactional public void read(UUID user,String slug,boolean value) { update(user,ContentKind.TUTORIAL,slug,null,value); }
    private void update(UUID user,ContentKind kind,String slug,Boolean favorite,Boolean read) {
        if (kind==ContentKind.QUESTION) throw ApiException.bad("独立题暂不支持收藏");
        lock(user);
        UUID id=(Boolean.TRUE.equals(favorite)||Boolean.TRUE.equals(read))?content.published(kind,slug).id():content.entry(kind,slug,false);
        if (jdbc.queryForObject("SELECT COUNT(*) FROM learning_records WHERE user_id=? AND content_id=?",Integer.class,user,id)==0) jdbc.update("INSERT INTO learning_records(user_id,content_id) VALUES (?,?)",user,id);
        if (favorite!=null) jdbc.update("UPDATE learning_records SET favorite=?,updated_at=CURRENT_TIMESTAMP WHERE user_id=? AND content_id=?",favorite,user,id);
        if (read!=null) jdbc.update("UPDATE learning_records SET read_at=CASE WHEN ? THEN COALESCE(read_at,CURRENT_TIMESTAMP) ELSE NULL END,updated_at=CURRENT_TIMESTAMP WHERE user_id=? AND content_id=?",read,user,id);
    }
    @Transactional public Summary merge(UUID user,LearningController.GuestImport data) {
        if (!data.confirmed()) throw ApiException.bad("迁移前须由用户明确确认合并");
        lock(user);
        for (var favorite:data.favorites()) favorite(user,ContentKind.parse(favorite.kind()),favorite.slug(),true);
        for (String slug:data.readArticles()) read(user,slug,true);
        for (var local:data.runs()) {
            var run=runs.begin(user,local.id(),local.storySlug());
            if (run.version()!=local.version()) throw ApiException.conflict("游客内容版本已改变，请保留本地记录并重新练习");
            for (int i=0;i<local.choices().size();i++) {
                var choice=local.choices().get(i); runs.choose(user,local.id(),i+1,choice.nodeId(),choice.optionIndex());
            }
        }
        return summary(user);
    }
}
