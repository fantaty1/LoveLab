package com.lovelab.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.lovelab.common.ApiException;
import com.lovelab.common.PageResult;
import com.lovelab.content.ContentKind;
import com.lovelab.content.ContentRepository;
import com.lovelab.content.ContentSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RunService {
    public record ChoiceView(int sequence, String nodeId, int optionIndex, JsonNode feedback) {}
    public record RunView(UUID id, String storySlug, int version, String currentNode, String status, int revision,
                          boolean available, OffsetDateTime updatedAt, List<ChoiceView> choices, JsonNode story) {}
    private final RunRepository runs;
    private final ContentRepository content;
    public RunService(RunRepository runs, ContentRepository content) { this.runs=runs; this.content=content; }
    @Transactional public RunView begin(UUID user, UUID id, String slug) {
        runs.lockUser(user);
        ContentSnapshot story = content.published(ContentKind.STORY,slug);
        if (runs.exists(id)) {
            var existing=runs.owned(id,user,true);
            if (!existing.contentId().equals(story.id())) throw ApiException.conflict("该局ID已用于其他剧场");
            return view(existing,true);
        }
        runs.create(id,user,story.id(),story.version(),story.document().path("scenes").get(0).path("id").asText());
        return get(user,id);
    }
    @Transactional public RunView choose(UUID user, UUID id, int sequence, String nodeId, int optionIndex) {
        var run = runs.owned(id,user,true);
        var previous = runs.choices(id).stream().filter(c -> c.sequence()==sequence).findFirst();
        if (previous.isPresent()) {
            if (!previous.get().nodeId().equals(nodeId) || previous.get().optionIndex()!=optionIndex) throw ApiException.conflict("该序号已提交其他选项，请重新读取存档");
            return view(run,true);
        }
        if (!content.isPublished(run.contentId())) throw new ApiException(410,"CONTENT_UNAVAILABLE","内容已下架，当前局暂停，请返回剧场");
        if (!run.status().equals("ACTIVE") || sequence != run.revision()+1 || !nodeId.equals(run.currentNode())) throw ApiException.conflict("剧情进度已改变，请重新读取存档");
        var snapshot=content.version(run.contentId(),run.version());
        JsonNode scene=findScene(snapshot.document(),nodeId);
        if (optionIndex<0 || optionIndex>=scene.path("choices").size()) throw ApiException.bad("选项不存在");
        JsonNode selected=scene.path("choices").get(optionIndex);
        runs.choose(id,new RunRepository.Selected(sequence,nodeId,optionIndex),selected.has("next")?selected.path("next").asText():null);
        return get(user,id);
    }
    @Transactional(readOnly=true) public RunView get(UUID user,UUID id) { return view(runs.owned(id,user,false),true); }
    @Transactional(readOnly=true) public PageResult<RunView> list(UUID user,int page,int size) {
        PageResult.validate(page,size);
        return new PageResult<>(runs.list(user,page,size).stream().map(id -> view(runs.owned(id,user,false),false)).toList(),page,size,runs.count(user));
    }
    private RunView view(RunRepository.Run run,boolean includeStory) {
        ContentSnapshot snapshot=content.version(run.contentId(),run.version());
        List<ChoiceView> choices=runs.choices(run.id()).stream().map(c -> new ChoiceView(c.sequence(),c.nodeId(),c.optionIndex(),findScene(snapshot.document(),c.nodeId()).path("choices").get(c.optionIndex()))).toList();
        return new RunView(run.id(),snapshot.slug(),run.version(),run.currentNode(),run.status(),run.revision(),content.isPublished(run.contentId()),run.updatedAt(),choices,includeStory?snapshot.document():null);
    }
    private JsonNode findScene(JsonNode story,String id) {
        for (JsonNode scene:story.path("scenes")) if (scene.path("id").asText().equals(id)) return scene;
        throw new IllegalStateException("Stored run refers to a missing scene");
    }
}
