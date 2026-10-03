package com.lovelab.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.lovelab.auth.Account;
import com.lovelab.common.PageResult;
import com.lovelab.content.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    public record Draft(@NotNull JsonNode document) {}
    public record Review(@NotBlank @Size(max=1000) String note) {}
    public record Schedule(@NotBlank @Size(max=80) String questionSlug,@Min(1) int version) {}
    public record Role(@NotBlank String role) {}
    private final AdminService admin;
    public AdminController(AdminService admin) { this.admin=admin; }
    @GetMapping("/content/{kind}") PageResult<Map<String,Object>> list(@PathVariable String kind,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) { return admin.list(ContentKind.parse(kind),page,size); }
    @GetMapping("/content/{kind}/{slug}/versions/{version}") ContentSnapshot version(@PathVariable String kind,@PathVariable String slug,@PathVariable int version) { return admin.get(ContentKind.parse(kind),slug,version); }
    @PostMapping("/content/{kind}/{slug}/versions") @ResponseStatus(HttpStatus.CREATED)
    ContentSnapshot draft(@AuthenticationPrincipal Account actor,@PathVariable String kind,@PathVariable String slug,@Valid @RequestBody Draft body) { return admin.draft(actor.id(),ContentKind.parse(kind),slug,body.document); }
    @PostMapping("/content/{kind}/{slug}/versions/{version}/review") @PreAuthorize("hasRole('ADMIN')")
    ContentSnapshot review(@AuthenticationPrincipal Account actor,@PathVariable String kind,@PathVariable String slug,@PathVariable int version,@Valid @RequestBody Review body) { return admin.review(actor.id(),ContentKind.parse(kind),slug,version,body.note); }
    @PostMapping("/content/{kind}/{slug}/versions/{version}/publish") @PreAuthorize("hasRole('ADMIN')")
    ContentSnapshot publish(@AuthenticationPrincipal Account actor,@PathVariable String kind,@PathVariable String slug,@PathVariable int version) { return admin.publish(actor.id(),ContentKind.parse(kind),slug,version); }
    @DeleteMapping("/content/{kind}/{slug}/publication") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT)
    void withdraw(@AuthenticationPrincipal Account actor,@PathVariable String kind,@PathVariable String slug) { admin.withdraw(actor.id(),ContentKind.parse(kind),slug); }
    @PutMapping("/daily/{day}") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT)
    void schedule(@AuthenticationPrincipal Account actor,@PathVariable LocalDate day,@Valid @RequestBody Schedule body) { admin.schedule(actor.id(),day,body.questionSlug,body.version); }
    @PutMapping("/users/{id}/role") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT)
    void role(@AuthenticationPrincipal Account actor,@PathVariable UUID id,@Valid @RequestBody Role body) { admin.role(actor.id(),id,body.role); }
    @GetMapping("/audit") @PreAuthorize("hasRole('ADMIN')") PageResult<Map<String,Object>> audit(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) { return admin.audit(page,size); }
}
