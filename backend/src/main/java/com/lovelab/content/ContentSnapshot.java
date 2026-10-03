package com.lovelab.content;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

public record ContentSnapshot(UUID id, ContentKind kind, String slug, int version, String status,
                              JsonNode document, UUID createdBy, UUID reviewedBy) {
    public ContentView publicView() { return new ContentView(kind, slug, version, document); }
    public record ContentView(ContentKind kind, String slug, int version, JsonNode document) {}
}
