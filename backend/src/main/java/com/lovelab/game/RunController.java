package com.lovelab.game;

import com.lovelab.auth.Account;
import com.lovelab.common.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/runs")
public class RunController {
    public record Start(@NotNull UUID id,@NotBlank @Pattern(regexp="[a-z0-9-]{1,80}") String storySlug) {}
    public record Select(@Min(1) @Max(100) int sequence,@NotBlank @Size(max=80) String nodeId,@Min(0) @Max(3) int optionIndex) {}
    private final RunService runs;
    public RunController(RunService runs) { this.runs=runs; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) RunService.RunView start(@AuthenticationPrincipal Account account,@Valid @RequestBody Start body) { return runs.begin(account.id(),body.id,body.storySlug); }
    @GetMapping PageResult<RunService.RunView> list(@AuthenticationPrincipal Account account,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) { return runs.list(account.id(),page,size); }
    @GetMapping("/{id}") RunService.RunView get(@AuthenticationPrincipal Account account,@PathVariable UUID id) { return runs.get(account.id(),id); }
    @PostMapping("/{id}/choices") RunService.RunView choose(@AuthenticationPrincipal Account account,@PathVariable UUID id,@Valid @RequestBody Select body) { return runs.choose(account.id(),id,body.sequence,body.nodeId,body.optionIndex); }
}
