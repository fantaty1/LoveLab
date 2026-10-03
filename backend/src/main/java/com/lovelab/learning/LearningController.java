package com.lovelab.learning;

import com.lovelab.auth.Account;
import com.lovelab.content.ContentKind;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me")
public class LearningController {
    public record Favorite(@NotBlank @Size(max=16) String kind,@NotBlank @Size(max=80) String slug) {}
    public record LocalChoice(@NotBlank @Size(max=80) String nodeId,@Min(0) @Max(3) int optionIndex) {}
    public record LocalRun(@NotNull UUID id,@NotBlank @Size(max=80) String storySlug,@Min(1) int version,@NotNull @Size(max=100) List<@Valid LocalChoice> choices) {}
    public record GuestImport(boolean confirmed,@NotNull @Size(max=100) List<@Valid Favorite> favorites,
                              @NotNull @Size(max=100) List<@NotBlank @Size(max=80) String> readArticles,
                              @NotNull @Size(max=30) List<@Valid LocalRun> runs) {}
    private final LearningService learning;
    public LearningController(LearningService learning) { this.learning=learning; }
    @GetMapping LearningService.Summary me(@AuthenticationPrincipal Account account) { return learning.summary(account.id()); }
    @PutMapping("/favorites/{kind}/{slug}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void favorite(@AuthenticationPrincipal Account account,@PathVariable String kind,@PathVariable String slug) { learning.favorite(account.id(),ContentKind.parse(kind),slug,true); }
    @DeleteMapping("/favorites/{kind}/{slug}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void unfavorite(@AuthenticationPrincipal Account account,@PathVariable String kind,@PathVariable String slug) { learning.favorite(account.id(),ContentKind.parse(kind),slug,false); }
    @PutMapping("/read/{slug}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void read(@AuthenticationPrincipal Account account,@PathVariable String slug) { learning.read(account.id(),slug,true); }
    @DeleteMapping("/read/{slug}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void unread(@AuthenticationPrincipal Account account,@PathVariable String slug) { learning.read(account.id(),slug,false); }
    @PostMapping("/import") LearningService.Summary merge(@AuthenticationPrincipal Account account,@Valid @RequestBody GuestImport body) { return learning.merge(account.id(),body); }
}
