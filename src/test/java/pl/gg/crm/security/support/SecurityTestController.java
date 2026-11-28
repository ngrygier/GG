package pl.gg.testsupport;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kontroler wyłącznie testowy - endpointy pokazujące typowe sposoby zabezpieczania API.
 */
@RestController
public class SecurityTestController {

    @GetMapping("/api/public/ping")
    public String publicPing() {
        return "public";
    }

    @PostMapping("/auth/login")
    public String login() {
        return "login";
    }

    @GetMapping("/api/secure/ping")
    public String securePing() {
        return "secure";
    }

    @PostMapping("/api/clients")
    @PreAuthorize("hasAuthority('CLIENT_CREATE')")
    public String createClient() {
        return "created";
    }

    @GetMapping("/api/tasks/{taskId}")
    @PreAuthorize("hasAuthority('TASK_VIEW') and @resourceAccess.canAccessTask(authentication, #taskId)")
    public String getTask(@PathVariable Long taskId) {
        return "task " + taskId;
    }
}
