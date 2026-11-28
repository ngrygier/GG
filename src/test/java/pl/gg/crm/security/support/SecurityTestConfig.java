package pl.gg.crm.security.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.gg.crm.config.CorsConfig;
import pl.gg.crm.config.SecurityConfig;
import pl.gg.crm.security.access.NotImplementedResourceAssignmentLookup;
import pl.gg.crm.security.access.ResourceAccessService;
import pl.gg.crm.security.jwt.JwtSecurityConfig;
import pl.gg.crm.security.user.CurrentUserIdResolver;

/**
 * Produkcyjna konfiguracja bezpieczeństwa (bez żadnych wyłączeń) do użycia w testach {@code @WebMvcTest}.
 */
@TestConfiguration
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        JwtSecurityConfig.class,
        CurrentUserIdResolver.class,
        ResourceAccessService.class,
        NotImplementedResourceAssignmentLookup.class
})
public class SecurityTestConfig {

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("No users are configured for MVC security tests");
        };
    }
}
