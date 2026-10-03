package com.lovelab.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonCodec {
    private final ObjectMapper mapper;
    public JsonCodec(ObjectMapper mapper) { this.mapper = mapper; }
    public JsonNode read(String text) {
        try { return mapper.readTree(text); }
        catch (Exception error) { throw new IllegalStateException("Stored content JSON is invalid", error); }
    }
    public String write(JsonNode node) { return node.toString(); }
}
