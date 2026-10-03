package com.lovelab.daily;

import com.lovelab.auth.Account;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/daily")
public class DailyController {
    public record Submit(@NotNull UUID id,@NotNull LocalDate date,@NotBlank @Size(max=80) String questionSlug,@Min(1) int version,@Min(0) @Max(3) int optionIndex) {}
    private final DailyService daily;
    public DailyController(DailyService daily) { this.daily=daily; }
    @GetMapping DailyService.Assignment today() { return daily.today(); }
    @PostMapping("/attempts") DailyService.Attempt submit(@AuthenticationPrincipal Account account,@Valid @RequestBody Submit body) { return daily.submit(account.id(),body.id,body.date,body.questionSlug,body.version,body.optionIndex); }
}
