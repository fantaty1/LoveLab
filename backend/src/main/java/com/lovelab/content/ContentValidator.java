package com.lovelab.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.lovelab.common.ApiException;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class ContentValidator {
    private void require(boolean condition, String message) { if (!condition) throw ApiException.bad(message); }
    private String text(JsonNode node, String field) {
        require(node.path(field).isTextual() && !node.path(field).asText().isBlank(), "缺少非空文本字段：" + field);
        return node.path(field).asText();
    }
    public void validate(ContentKind kind, String slug, JsonNode document) {
        require(slug.matches("[a-z0-9][a-z0-9-]{0,79}"), "slug只允许小写字母、数字与连字符");
        require(document != null && document.isObject(), "document必须是JSON对象");
        require(document.toString().getBytes(StandardCharsets.UTF_8).length <= 524288, "单条内容不能超过512KB");
        require(text(document, kind == ContentKind.TUTORIAL ? "slug" : "id").equals(slug), "文档标识须与路径slug一致");
        switch (kind) {
            case STORY -> {
                text(document, "title"); text(document, "description");
                JsonNode scenes = document.path("scenes");
                require(scenes.isArray() && scenes.size() > 0 && scenes.size() <= 100, "剧场须包含1至100幕");
                Map<String, JsonNode> nodes = new HashMap<>();
                for (JsonNode scene : scenes) {
                    String id = text(scene, "id"); require(id.matches("[a-z0-9-]{1,80}"), "节点ID格式不正确");
                    require(nodes.put(id, scene) == null, "节点ID重复"); validateScene(scene, false);
                }
                for (JsonNode node : scenes) for (JsonNode choice : node.path("choices")) if (choice.has("next")) {
                    require(choice.path("next").isTextual() && nodes.containsKey(choice.path("next").asText()), "下一幕引用不存在");
                }
                Set<String> visited = new HashSet<>(); walk(scenes.get(0).path("id").asText(), nodes, visited, new HashSet<>());
                require(visited.size() == nodes.size(), "存在不可到达的节点");
            }
            case QUESTION -> validateScene(document, true);
            case CARD -> { for (String field : List.of("title", "intro", "action", "example", "caution", "source", "url")) text(document, field); require(document.path("url").asText().startsWith("https://"), "来源链接须为HTTPS"); }
            case TUTORIAL -> {
                for (String field : List.of("title", "description", "topic", "takeaway", "stage")) text(document, field);
                require(document.path("sections").isArray() && document.path("sections").size() > 0, "缺少正文小节");
                Set<String> anchors = new HashSet<>(List.of("article-start", "example", "pitfalls", "practice", "sources"));
                for (JsonNode section : document.path("sections")) {
                    String id = text(section, "id"); require(id.matches("[a-z][a-z0-9-]{0,79}") && anchors.add(id), "正文目录锚点格式不正确或重复");
                    text(section, "title"); require(section.path("paragraphs").isArray() && !section.path("paragraphs").isEmpty(), "正文小节不能为空");
                    for (JsonNode paragraph : section.path("paragraphs")) require(paragraph.isTextual() && !paragraph.asText().isBlank(), "正文段落须为非空文本");
                }
                for (String field : List.of("situation", "before", "after", "explanation")) text(document.path("example"), field);
                for (String field : List.of("prompt", "answer", "reason")) text(document.path("practice"), field);
                require(document.path("pitfalls").isArray() && !document.path("pitfalls").isEmpty(), "缺少误区提示");
                require(document.path("refs").isArray() && !document.path("refs").isEmpty(), "缺少来源记录");
                for (JsonNode ref : document.path("refs")) { text(ref, "source"); text(ref, "note"); }
            }
        }
    }
    private void validateScene(JsonNode scene, boolean question) {
        for (String field : List.of("line", "context", "aside", "speaker")) text(scene, field);
        JsonNode choices = scene.path("choices");
        require(choices.isArray() && choices.size() >= 2 && choices.size() <= 4, "每幕须包含2至4个选项");
        for (JsonNode choice : choices) {
            for (String field : List.of("text", "rating", "response", "explanation", "card", "skill")) text(choice, field);
            require(!question || !choice.has("next"), "独立题不能包含下一幕");
        }
    }
    private void walk(String id, Map<String, JsonNode> nodes, Set<String> visited, Set<String> active) {
        require(!active.contains(id), "首版剧情不允许循环分支");
        if (visited.contains(id)) return;
        active.add(id);
        for (JsonNode choice : nodes.get(id).path("choices")) if (choice.has("next")) walk(choice.path("next").asText(), nodes, visited, active);
        active.remove(id); visited.add(id);
    }
}
