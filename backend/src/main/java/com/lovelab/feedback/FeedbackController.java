package com.lovelab.feedback;

import com.lovelab.auth.Account;
import com.lovelab.common.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class FeedbackController {
    public record Submit(@NotBlank @Size(max=16) String kind,@NotBlank @Size(max=80) String slug,@NotBlank String category,@NotBlank @Size(max=1000) String message) {}
    public record Resolve(@NotBlank @Size(max=1000) String note) {}
    private final FeedbackService feedback;
    public FeedbackController(FeedbackService feedback) { this.feedback=feedback; }
    @PostMapping("/feedback") @ResponseStatus(HttpStatus.CREATED)
    Map<String,UUID> submit(@AuthenticationPrincipal Account account,@Valid @RequestBody Submit body) { return Map.of("id",feedback.submit(account==null?null:account.id(),body)); }
    @GetMapping("/admin/feedback") PageResult<Map<String,Object>> list(@RequestParam(defaultValue="OPEN") String status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) { return feedback.list(status,page,size); }
    @PutMapping("/admin/feedback/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void resolve(@AuthenticationPrincipal Account actor,@PathVariable UUID id,@Valid @RequestBody Resolve body) { feedback.resolve(actor.id(),id,body.note); }
}
