package com.lovelab.feedback;

import com.lovelab.common.ApiException;
import com.lovelab.common.PageResult;
import com.lovelab.content.ContentKind;
import com.lovelab.content.ContentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FeedbackService {
    private final JdbcTemplate jdbc;
    private final ContentRepository content;
    public FeedbackService(JdbcTemplate jdbc,ContentRepository content) { this.jdbc=jdbc; this.content=content; }
    @Transactional public UUID submit(UUID user,FeedbackController.Submit body) {
        if (!Set.of("UNCLEAR","MISSING_CONTEXT","CONTENT_ISSUE").contains(body.category())) throw ApiException.bad("反馈类型不正确");
        UUID id=UUID.randomUUID();
        jdbc.update("INSERT INTO content_feedback(id,user_id,content_id,category,message) VALUES (?,?,?,?,?)",id,user,content.published(ContentKind.parse(body.kind()),body.slug()).id(),body.category(),body.message().trim()); return id;
    }
    public PageResult<Map<String,Object>> list(String status,int page,int size) {
        PageResult.validate(page,size);
        if (!Set.of("OPEN","RESOLVED").contains(status)) throw ApiException.bad("反馈状态不正确");
        return new PageResult<>(jdbc.queryForList("SELECT f.*,e.kind,e.slug FROM content_feedback f JOIN content_entries e ON e.id=f.content_id WHERE f.status=? ORDER BY f.created_at DESC,f.id LIMIT ? OFFSET ?",status,size,page*size),page,size,jdbc.queryForObject("SELECT COUNT(*) FROM content_feedback WHERE status=?",Long.class,status));
    }
    @Transactional public void resolve(UUID actor,UUID id,String note) {
        if (jdbc.update("UPDATE content_feedback SET status='RESOLVED',resolution=? WHERE id=?",note,id)==0) throw ApiException.missing();
        content.audit(actor,"FEEDBACK_RESOLVE",id.toString(),note);
    }
}
