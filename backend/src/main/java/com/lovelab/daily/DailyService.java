package com.lovelab.daily;

import com.fasterxml.jackson.databind.JsonNode;
import com.lovelab.common.ApiException;
import com.lovelab.content.ContentKind;
import com.lovelab.content.ContentRepository;
import com.lovelab.content.ContentSnapshot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class DailyService {
    public record Assignment(LocalDate date,ContentSnapshot.ContentView question,boolean fallback) {}
    public record Attempt(UUID id,LocalDate date,String questionSlug,int version,int optionIndex,JsonNode feedback) {}
    private final Clock clock;
    private final JdbcTemplate jdbc;
    private final ContentRepository content;
    public DailyService(Clock clock,JdbcTemplate jdbc,ContentRepository content) { this.clock=clock; this.jdbc=jdbc; this.content=content; }
    @Transactional public Assignment today() { return assignment(LocalDate.now(clock)); }
    private Assignment assignment(LocalDate date) {
        // Serialize first fallback assignment with scheduling, then pin its content version for this date.
        jdbc.queryForObject("SELECT id FROM content_metadata WHERE id='handbook' FOR UPDATE",String.class);
        var scheduled=jdbc.query("SELECT d.content_id,d.version,d.is_fallback FROM daily_assignments d JOIN content_entries e ON e.id=d.content_id WHERE d.practice_date=? AND e.published_version IS NOT NULL", (rs,row)->new Assignment(date,content.version(rs.getObject(1,UUID.class),rs.getInt(2)).publicView(),rs.getBoolean(3)),date);
        if (!scheduled.isEmpty()) return scheduled.get(0);
        var fallback=jdbc.query("SELECT id,published_version FROM content_entries WHERE kind='QUESTION' AND published_version IS NOT NULL ORDER BY slug LIMIT 1",(rs,row)->content.version(rs.getObject(1,UUID.class),rs.getInt(2)));
        if (fallback.isEmpty()) throw new ApiException(404,"NO_DAILY_QUESTION","今日暂无可用题目");
        jdbc.update("DELETE FROM daily_assignments WHERE practice_date=?",date);
        jdbc.update("INSERT INTO daily_assignments(practice_date,content_id,version,is_fallback) VALUES (?,?,?,TRUE)",date,fallback.get(0).id(),fallback.get(0).version());
        return new Assignment(date,fallback.get(0).publicView(),true);
    }
    @Transactional public Attempt submit(UUID user,UUID id,LocalDate date,String slug,int version,int index) {
        // One account row serializes duplicate attempt IDs without trusting a supplied outcome.
        jdbc.queryForObject("SELECT id FROM app_users WHERE id=? FOR UPDATE",UUID.class,user);
        var old=jdbc.query("SELECT * FROM daily_attempts WHERE id=? AND user_id=?", (rs,row)->new Attempt(id,rs.getObject("practice_date",LocalDate.class),content.version(rs.getObject("content_id",UUID.class),rs.getInt("version")).slug(),rs.getInt("version"),rs.getInt("option_index"),null),id,user);
        if (!old.isEmpty()) {
            Attempt previous=old.get(0);
            if (!previous.date.equals(date) || !previous.questionSlug.equals(slug) || previous.version!=version || previous.optionIndex!=index) throw ApiException.conflict("尝试ID已用于另一次提交");
            return feedback(previous);
        }
        if (!date.equals(LocalDate.now(clock))) throw ApiException.conflict("日期已改变，请重新读取今日题");
        Assignment assigned=assignment(date);
        if (!assigned.question.slug().equals(slug) || assigned.question.version()!=version) throw ApiException.conflict("题目已改变，请重新读取今日题");
        if (index<0 || index>=assigned.question.document().path("choices").size()) throw ApiException.bad("选项不存在");
        UUID contentId=content.entry(ContentKind.QUESTION,slug,false);
        jdbc.update("INSERT INTO daily_attempts(id,user_id,practice_date,content_id,version,option_index) VALUES (?,?,?,?,?,?)",id,user,date,contentId,version,index);
        return new Attempt(id,date,slug,version,index,assigned.question.document().path("choices").get(index));
    }
    private Attempt feedback(Attempt attempt) {
        var snapshot=content.version(content.entry(ContentKind.QUESTION,attempt.questionSlug,false),attempt.version);
        return new Attempt(attempt.id,attempt.date,attempt.questionSlug,attempt.version,attempt.optionIndex,snapshot.document().path("choices").get(attempt.optionIndex));
    }
}
