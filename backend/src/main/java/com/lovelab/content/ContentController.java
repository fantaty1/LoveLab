package com.lovelab.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.lovelab.common.PageResult;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/content")
public class ContentController {
    private final ContentRepository content;
    public ContentController(ContentRepository content) { this.content = content; }
    @GetMapping("/metadata") JsonNode metadata() { return content.metadata(); }
    @GetMapping("/{kind}") PageResult<ContentSnapshot.ContentView> list(@PathVariable String kind,
            @RequestParam(defaultValue="") @Size(max=100) String q, @RequestParam(defaultValue="") @Size(max=80) String topic,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return content.list(ContentKind.parse(kind), q.trim(), topic, page, size);
    }
    @GetMapping("/{kind}/{slug}") ContentSnapshot.ContentView detail(@PathVariable String kind, @PathVariable String slug) {
        return content.published(ContentKind.parse(kind), slug).publicView();
    }
}
