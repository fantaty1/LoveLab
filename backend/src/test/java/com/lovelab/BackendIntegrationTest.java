package com.lovelab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lovelab.auth.*;
import com.lovelab.content.*;
import com.lovelab.game.RunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendIntegrationTest {
    private static final String PASSWORD="Testing-password-123";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired AccountRepository accounts;
    @Autowired PasswordEncoder encoder;
    @Autowired ContentRepository content;
    @Autowired JdbcTemplate jdbc;
    @Autowired RunService runs;
    private Account user(String role) { return accounts.insert("test_"+UUID.randomUUID().toString().replace("-","").substring(0,16),encoder.encode(PASSWORD),role); }
    private MockHttpSession login(Account user) throws Exception {
        MvcResult response=mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("username",user.username(),"password",PASSWORD)))).andExpect(status().isOk()).andReturn();
        return (MockHttpSession)response.getRequest().getSession(false);
    }
    private JsonNode request(MockHttpServletRequestBuilder request,int status) throws Exception {
        var result=mvc.perform(request).andExpect(status().is(status)).andReturn();
        return result.getResponse().getContentAsString().isBlank()?mapper.nullNode():mapper.readTree(result.getResponse().getContentAsString());
    }
    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request,Object body) throws Exception { return request.with(csrf()).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)); }
    private Map<String,Object> select(int sequence,String node,int option) { return Map.of("sequence",sequence,"nodeId",node,"optionIndex",option); }

    @Test void publicContentIsPaginatedSearchableAndHasAllTutorials() throws Exception {
        request(get("/api/v1/content/TUTORIAL?size=50"),200).path("total").asInt();
        assertThat(request(get("/api/v1/content/TUTORIAL?size=50"),200).path("items").size()).isEqualTo(20);
        assertThat(request(get("/api/v1/content/TUTORIAL?topic=emotion"),200).path("total").asInt()).isEqualTo(3);
        assertThat(request(get("/api/v1/content/TUTORIAL?q=想牵手"),200).path("items").get(0).path("slug").asText()).isEqualTo("consensual-touch");
        request(get("/api/v1/content/TUTORIAL?size=500"),400);
        request(get("/api/v1/content/TUTORIAL/missing"),404);
        assertThat(request(get("/api/v1/content/metadata"),200).path("topics").size()).isEqualTo(6);
        assertThat(request(get("/actuator/health"),200).path("status").asText()).isEqualTo("UP");
    }
    @Test void actualSessionLoginRotatesCsrfAndLogoutInvalidatesSession() throws Exception {
        var account=user("USER");
        var initial=mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        var session=(MockHttpSession)initial.getRequest().getSession(false);
        String token=mapper.readTree(initial.getResponse().getContentAsString()).path("token").asText();
        mvc.perform(post("/api/v1/auth/login").session(session).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("username",account.username(),"password",PASSWORD)))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/auth/login").session(session).header("X-CSRF-TOKEN",token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("username",account.username(),"password",PASSWORD)))).andExpect(status().isOk());
        assertThat(request(get("/api/v1/auth/me").session(session),200).path("username").asText()).isEqualTo(account.username());
        mvc.perform(put("/api/v1/me/read/emotional-validation").session(session).header("X-CSRF-TOKEN",token)).andExpect(status().isForbidden());
        String renewed=request(get("/api/v1/auth/csrf").session(session),200).path("token").asText();
        mvc.perform(post("/api/v1/auth/logout").session(session).header("X-CSRF-TOKEN",renewed)).andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
        request(get("/api/v1/me"),401);
    }
    @Test void registrationHashesPasswordAndRejectsInvalidAndDuplicateUsers() throws Exception {
        String username="register_"+UUID.randomUUID().toString().substring(0,8);
        request(json(post("/api/v1/auth/register"),Map.of("username",username,"password",PASSWORD)),201);
        var stored=accounts.find(username).orElseThrow();
        assertThat(stored.password()).isNotEqualTo(PASSWORD); assertThat(encoder.matches(PASSWORD,stored.password())).isTrue();
        request(json(post("/api/v1/auth/register"),Map.of("username",username.toUpperCase(),"password",PASSWORD)),409);
        request(json(post("/api/v1/auth/register"),Map.of("username","invalid","password","short")),400);
        request(json(post("/api/v1/auth/register"),Map.of("username","invalid","password","中".repeat(30))),400);
        request(json(post("/api/v1/auth/login"),Map.of("username",username,"password","incorrect-password")),401);
    }
    @Test void runChoicesAreAuthoritativeIdempotentAndCannotSkipScenes() throws Exception {
        var owner=user("USER"); var session=login(owner); UUID id=UUID.randomUUID();
        var start=Map.of("id",id,"storySlug","coffee");
        request(json(post("/api/v1/runs").session(session),start),201);
        request(json(post("/api/v1/runs").session(session),start),201);
        request(json(post("/api/v1/runs/"+id+"/choices").session(session),select(1,"invite",0)),409);
        request(json(post("/api/v1/runs/"+id+"/choices").session(session),select(2,"hello",0)),409);
        var first=request(json(post("/api/v1/runs/"+id+"/choices").session(session),select(1,"hello",0)),200);
        assertThat(first.path("choices").get(0).path("feedback").path("rating").asText()).isEqualTo("自然又具体");
        assertThat(request(json(post("/api/v1/runs/"+id+"/choices").session(session),select(1,"hello",0)),200).path("revision").asInt()).isEqualTo(1);
        request(json(post("/api/v1/runs/"+id+"/choices").session(session),select(1,"hello",1)),409);
        var forged=new HashMap<>(select(2,"share",0)); forged.put("rating","满分");
        request(json(post("/api/v1/runs/"+id+"/choices").session(session),forged),400);
        request(json(post("/api/v1/runs/"+id+"/choices").session(session),select(2,"share",0)),200);
        request(json(post("/api/v1/runs/"+id+"/choices").session(session),select(3,"invite",0)),200);
        var completed=request(json(post("/api/v1/runs/"+id+"/choices").session(session),select(4,"confirm",0)),200);
        assertThat(completed.path("status").asText()).isEqualTo("COMPLETE");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM run_choices WHERE run_id=?",Integer.class,id)).isEqualTo(4);
        request(json(post("/api/v1/runs/"+id+"/choices").session(session),select(5,"hello",0)),409);
        request(get("/api/v1/runs/"+id).session(login(user("USER"))),404);
    }
    @Test void concurrentDuplicateChoicesCreateExactlyOneRecord() throws Exception {
        var owner=user("USER"); UUID id=UUID.randomUUID(); runs.begin(owner.id(),id,"listening");
        var executor=Executors.newFixedThreadPool(2);
        try {
            List<Callable<RunService.RunView>> tasks=List.of(()->runs.choose(owner.id(),id,1,"tired",0),()->runs.choose(owner.id(),id,1,"tired",0));
            for (var result:executor.invokeAll(tasks)) assertThat(result.get().revision()).isEqualTo(1);
        } finally { executor.shutdownNow(); }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM run_choices WHERE run_id=?",Integer.class,id)).isEqualTo(1);
    }
    @Test void contentLifecycleKeepsOldRunsPinnedAndWithdrawnContentPrivate() throws Exception {
        var editor=login(user("EDITOR")); var admin=login(user("ADMIN")); var player=login(user("USER"));
        String slug="story-"+UUID.randomUUID().toString().substring(0,8);
        ObjectNode document=(ObjectNode)content.published(ContentKind.STORY,"listening").document().deepCopy(); document.put("id",slug);
        var draftUrl="/api/v1/admin/content/STORY/"+slug+"/versions";
        request(json(post(draftUrl).session(editor),Map.of("document",document)),201);
        request(get("/api/v1/content/STORY/"+slug),404);
        request(post(draftUrl+"/1/publish").session(admin).with(csrf()),409);
        request(json(post(draftUrl+"/1/review").session(editor),Map.of("note","编辑无审核权限")),403);
        request(json(post(draftUrl+"/1/review").session(admin),Map.of("note","来源、情景和分支已检查")),200);
        request(post(draftUrl+"/1/publish").session(admin).with(csrf()),200);
        UUID id=UUID.randomUUID(); request(json(post("/api/v1/runs").session(player),Map.of("id",id,"storySlug",slug)),201);
        document.put("title","新版测试");
        request(json(post(draftUrl).session(editor),Map.of("document",document)),201);
        request(json(post(draftUrl+"/2/review").session(admin),Map.of("note","第二版已审核")),200);
        request(post(draftUrl+"/2/publish").session(admin).with(csrf()),200);
        assertThat(request(get("/api/v1/content/STORY/"+slug),200).path("version").asInt()).isEqualTo(2);
        var saved=request(json(post("/api/v1/runs/"+id+"/choices").session(player),select(1,"tired",0)),200);
        assertThat(saved.path("version").asInt()).isEqualTo(1);
        request(delete("/api/v1/admin/content/STORY/"+slug+"/publication").session(admin).with(csrf()),204);
        request(get("/api/v1/content/STORY/"+slug),404);
        assertThat(request(get("/api/v1/runs/"+id).session(player),200).path("available").asBoolean()).isFalse();
        request(json(post("/api/v1/runs/"+id+"/choices").session(player),select(2,"time",0)),410);
        request(post(draftUrl+"/1/publish").session(admin).with(csrf()),200);
        assertThat(request(get("/api/v1/content/STORY/"+slug),200).path("version").asInt()).isEqualTo(1);
    }
    @Test void invalidGraphsAndSelfReviewAreRejected() throws Exception {
        var admin=login(user("ADMIN"));
        String slug="invalid-"+UUID.randomUUID().toString().substring(0,8);
        ObjectNode document=(ObjectNode)content.published(ContentKind.STORY,"listening").document().deepCopy(); document.put("id",slug);
        ((ObjectNode)document.path("scenes").get(0).path("choices").get(0)).put("next","tired");
        String url="/api/v1/admin/content/STORY/"+slug+"/versions";
        request(json(post(url).session(admin),Map.of("document",document)),400);
        ((ObjectNode)document.path("scenes").get(0).path("choices").get(0)).put("next","time");
        request(json(post(url).session(admin),Map.of("document",document)),201);
        request(json(post(url+"/1/review").session(admin),Map.of("note","自己审核")),409);
        request(get(url+"/1").session(login(user("USER"))),403);
    }
    @Test void favoritesAndReadMarkersPersistAndAreIdempotent() throws Exception {
        var session=login(user("USER"));
        for (int i=0;i<2;i++) {
            request(put("/api/v1/me/favorites/TUTORIAL/emotional-validation").session(session).with(csrf()),204);
            request(put("/api/v1/me/read/emotional-validation").session(session).with(csrf()),204);
        }
        var me=request(get("/api/v1/me").session(session),200);
        assertThat(me.path("favorites").asInt()).isEqualTo(1); assertThat(me.path("readTutorials").asInt()).isEqualTo(1);
        request(delete("/api/v1/me/read/emotional-validation").session(session).with(csrf()),204);
        request(delete("/api/v1/me/favorites/TUTORIAL/emotional-validation").session(session).with(csrf()),204);
        assertThat(request(get("/api/v1/me").session(session),200).path("records").isEmpty()).isTrue();
    }
    @Test void guestImportRequiresConfirmationAndRollsBackInvalidPaths() throws Exception {
        var session=login(user("USER")); UUID id=UUID.randomUUID();
        var favorite=Map.of("kind","CARD","slug","listen");
        var local=Map.of("id",id,"storySlug","listening","version",1,"choices",List.of(Map.of("nodeId","time","optionIndex",0)));
        var data=new HashMap<String,Object>(Map.of("confirmed",false,"favorites",List.of(favorite),"readArticles",List.of("emotional-validation"),"runs",List.of(local)));
        request(json(post("/api/v1/me/import").session(session),data),400);
        data.put("confirmed",true); request(json(post("/api/v1/me/import").session(session),data),409);
        assertThat(request(get("/api/v1/me").session(session),200).path("favorites").asInt()).isZero();
        request(get("/api/v1/runs/"+id).session(session),404);
        data.put("runs",List.of(Map.of("id",id,"storySlug","listening","version",1,"choices",List.of(Map.of("nodeId","tired","optionIndex",0)))));
        request(json(post("/api/v1/me/import").session(session),data),200);
        request(json(post("/api/v1/me/import").session(session),data),200);
        assertThat(request(get("/api/v1/runs/"+id).session(session),200).path("revision").asInt()).isEqualTo(1);
    }
    @Test void dailyAssignmentIsPinnedAndAttemptsAreValidated() throws Exception {
        var session=login(user("USER"));
        var daily=request(get("/api/v1/daily"),200);
        assertThat(request(get("/api/v1/daily"),200)).isEqualTo(daily);
        var data=new HashMap<String,Object>(Map.of("id",UUID.randomUUID(),"date",daily.path("date").asText(),"questionSlug",daily.path("question").path("slug").asText(),"version",daily.path("question").path("version").asInt(),"optionIndex",0));
        request(json(post("/api/v1/daily/attempts").session(session),data),200);
        request(json(post("/api/v1/daily/attempts").session(session),data),200);
        data.put("optionIndex",1); request(json(post("/api/v1/daily/attempts").session(session),data),409);
        data.put("id",UUID.randomUUID()); data.put("date",LocalDate.parse(daily.path("date").asText()).minusDays(1).toString());
        request(json(post("/api/v1/daily/attempts").session(session),data),409);
        assertThat(request(get("/api/v1/me").session(session),200).path("practiceDays").asInt()).isEqualTo(1);
    }
    @Test void feedbackCanBeSubmittedAndResolvedWithAnAuditRecord() throws Exception {
        var submitted=request(json(post("/api/v1/feedback"),Map.of("kind","TUTORIAL","slug","emotional-validation","category","UNCLEAR","message","这里想看更具体的例子。")),201);
        var editor=login(user("EDITOR"));
        request(json(put("/api/v1/admin/feedback/"+submitted.path("id").asText()).session(editor),Map.of("note","已补充示例并进入复审")),204);
        assertThat(jdbc.queryForObject("SELECT status FROM content_feedback WHERE id=?",String.class,UUID.fromString(submitted.path("id").asText()))).isEqualTo("RESOLVED");
    }
    @Test void revokedAdminRoleTakesEffectForAnExistingSession() throws Exception {
        var account=user("ADMIN"); var session=login(account);
        request(get("/api/v1/admin/audit").session(session),200);
        jdbc.update("UPDATE app_users SET role='USER' WHERE id=?",account.id());
        request(get("/api/v1/admin/audit").session(session),403);
    }
    @Test void corsRejectsUnknownOriginsAndPreflightSupportsTheFrontend() throws Exception {
        mvc.perform(options("/api/v1/me").header("Origin","http://127.0.0.1:5173").header("Access-Control-Request-Method","PUT").header("Access-Control-Request-Headers","X-CSRF-TOKEN")).andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Credentials","true"));
        mvc.perform(get("/api/v1/content/TUTORIAL").header("Origin","https://unknown.example")).andExpect(status().isForbidden());
    }
}
