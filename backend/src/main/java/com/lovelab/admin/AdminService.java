package com.lovelab.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.lovelab.common.ApiException;
import com.lovelab.common.JsonCodec;
import com.lovelab.common.PageResult;
import com.lovelab.content.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

@Service
public class AdminService {
    private final JdbcTemplate jdbc;
    private final ContentRepository content;
    private final ContentValidator validator;
    private final JsonCodec json;
    private final Clock clock;
    public AdminService(JdbcTemplate jdbc,ContentRepository content,ContentValidator validator,JsonCodec json,Clock clock) {
        this.jdbc=jdbc; this.content=content; this.validator=validator; this.json=json; this.clock=clock;
    }
    public PageResult<Map<String,Object>> list(ContentKind kind,int page,int size) {
        PageResult.validate(page,size);
        return new PageResult<>(jdbc.queryForList("SELECT e.kind,e.slug,e.published_version,MAX(v.version) AS latest_version FROM content_entries e JOIN content_versions v ON v.content_id=e.id WHERE e.kind=? GROUP BY e.id,e.kind,e.slug,e.published_version ORDER BY e.slug LIMIT ? OFFSET ?",kind.name(),size,page*size),page,size,jdbc.queryForObject("SELECT COUNT(*) FROM content_entries WHERE kind=?",Long.class,kind.name()));
    }
    public ContentSnapshot get(ContentKind kind,String slug,int version) { return content.version(content.entry(kind,slug,false),version); }
    @Transactional public ContentSnapshot draft(UUID actor,ContentKind kind,String slug,JsonNode document) {
        validator.validate(kind,slug,document);
        var ids=jdbc.query("SELECT id FROM content_entries WHERE kind=? AND slug=? FOR UPDATE",(rs,row)->rs.getObject(1,UUID.class),kind.name(),slug);
        UUID id;
        if (ids.isEmpty()) { id=UUID.randomUUID(); jdbc.update("INSERT INTO content_entries(id,kind,slug) VALUES (?,?,?)",id,kind.name(),slug); }
        else id=ids.get(0);
        int version=jdbc.queryForObject("SELECT COALESCE(MAX(version),0)+1 FROM content_versions WHERE content_id=?",Integer.class,id);
        jdbc.update("INSERT INTO content_versions(content_id,version,document,status,created_by) VALUES (?,?,?,'DRAFT',?)",id,version,json.write(document),actor);
        content.audit(actor,"DRAFT",kind+"/"+slug+"/"+version,"创建不可变版本");
        return content.version(id,version);
    }
    @Transactional public ContentSnapshot review(UUID actor,ContentKind kind,String slug,int version,String note) {
        UUID id=content.entry(kind,slug,true); var snapshot=content.version(id,version);
        if (!snapshot.status().equals("DRAFT")) throw ApiException.conflict("只有草稿可以审核");
        if (actor.equals(snapshot.createdBy())) throw ApiException.conflict("作者不能审核自己的草稿，请由另一位管理员审核");
        validator.validate(kind,slug,snapshot.document()); validateReferences(kind,snapshot.document());
        jdbc.update("UPDATE content_versions SET status='REVIEWED',reviewed_by=?,review_note=? WHERE content_id=? AND version=?",actor,note,id,version);
        content.audit(actor,"REVIEW",kind+"/"+slug+"/"+version,note);
        return content.version(id,version);
    }
    @Transactional public ContentSnapshot publish(UUID actor,ContentKind kind,String slug,int version) {
        UUID id=content.entry(kind,slug,true); var snapshot=content.version(id,version);
        if (!Set.of("REVIEWED","PUBLISHED").contains(snapshot.status())) throw ApiException.conflict("内容须先通过审核");
        // Revalidate dependencies when publishing or rolling back, as referenced content may have been withdrawn.
        validator.validate(kind,slug,snapshot.document()); validateReferences(kind,snapshot.document());
        jdbc.update("UPDATE content_versions SET status='PUBLISHED',published_at=COALESCE(published_at,CURRENT_TIMESTAMP) WHERE content_id=? AND version=?",id,version);
        jdbc.update("UPDATE content_entries SET published_version=? WHERE id=?",version,id);
        content.audit(actor,"PUBLISH",kind+"/"+slug+"/"+version,"切换公开版本"); return content.version(id,version);
    }
    @Transactional public void withdraw(UUID actor,ContentKind kind,String slug) {
        UUID id=content.entry(kind,slug,true); jdbc.update("UPDATE content_entries SET published_version=NULL WHERE id=?",id);
        content.audit(actor,"WITHDRAW",kind+"/"+slug,"停止公开访问与新局，保留历史版本");
    }
    private void validateReferences(ContentKind kind,JsonNode document) {
        if (kind==ContentKind.TUTORIAL) {
            JsonNode metadata=content.metadata();
            boolean topic=false;
            for (JsonNode value:metadata.path("topics")) if (value.path("id").asText().equals(document.path("topic").asText())) topic=true;
            if (!topic) throw ApiException.bad("教程主题不存在");
            for (JsonNode ref:document.path("refs")) if (!metadata.path("sources").has(ref.path("source").asText())) throw ApiException.bad("教程来源引用不存在");
            for (JsonNode related:document.path("related")) content.published(ContentKind.TUTORIAL,related.asText());
            if (document.has("story")) content.published(ContentKind.STORY,document.path("story").asText());
        }
        if (kind==ContentKind.STORY) for (JsonNode scene:document.path("scenes")) for (JsonNode choice:scene.path("choices")) content.published(ContentKind.CARD,choice.path("card").asText());
        if (kind==ContentKind.QUESTION) for (JsonNode choice:document.path("choices")) content.published(ContentKind.CARD,choice.path("card").asText());
    }
    @Transactional public void schedule(UUID actor,LocalDate day,String slug,int version) {
        if (day.isBefore(LocalDate.now(clock)) || day.isAfter(LocalDate.now(clock).plusDays(366))) throw ApiException.bad("排期须为今天至未来366天");
        var published=content.published(ContentKind.QUESTION,slug);
        if (published.version()!=version) throw ApiException.conflict("排期只能使用当前公开版本");
        jdbc.queryForObject("SELECT id FROM content_metadata WHERE id='handbook' FOR UPDATE",String.class);
        if (jdbc.queryForObject("SELECT COUNT(*) FROM daily_attempts WHERE practice_date=?",Integer.class,day)>0) throw ApiException.conflict("当天已有答题记录，不能更换题目");
        jdbc.update("DELETE FROM daily_assignments WHERE practice_date=?",day);
        jdbc.update("INSERT INTO daily_assignments(practice_date,content_id,version,is_fallback) VALUES (?,?,?,FALSE)",day,published.id(),version);
        content.audit(actor,"SCHEDULE",day.toString(),slug+"/"+version);
    }
    @Transactional public void role(UUID actor,UUID user,String role) {
        if (!Set.of("USER","EDITOR","ADMIN").contains(role)) throw ApiException.bad("角色不正确");
        if (actor.equals(user)) throw ApiException.bad("不能修改自己的角色");
        if (jdbc.update("UPDATE app_users SET role=? WHERE id=?",role,user)==0) throw ApiException.missing();
        content.audit(actor,"ROLE",user.toString(),role);
    }
    public PageResult<Map<String,Object>> audit(int page,int size) {
        PageResult.validate(page,size);
        return new PageResult<>(jdbc.queryForList("SELECT * FROM admin_audit ORDER BY created_at DESC,id LIMIT ? OFFSET ?",size,page*size),page,size,jdbc.queryForObject("SELECT COUNT(*) FROM admin_audit",Long.class));
    }
}
