package pl.gg.crm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

/**
 * Konfiguracja CORS ({@code crm.security.cors.*} w application.properties).
 *
 * <p>Brak skonfigurowanych originów oznacza, że żadne żądanie cross-origin nie zostanie przepuszczone.
 * Origin {@code *} jest niedozwolony, bo endpointy {@code /auth/**} działają z credentials (cookie z refresh tokenem).
 */
@ConfigurationProperties("crm.security.cors")
public record CorsProperties(
        @DefaultValue List<String> allowedOrigins,
        @DefaultValue({"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"}) List<String> allowedMethods,
        @DefaultValue({"Authorization", "Content-Type", "Accept"}) List<String> allowedHeaders,
        @DefaultValue("Location") List<String> exposedHeaders,
        @DefaultValue("1h") Duration maxAge
) {
}
