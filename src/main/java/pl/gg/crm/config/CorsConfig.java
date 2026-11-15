package pl.gg.crm.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS dla frontendu React (i przyszłych klientów webowych). Wartości pochodzą z {@link CorsProperties},
 * więc zmiana originów nie wymaga zmian w kodzie.
 *
 * <ul>
 *     <li>{@code /api/**} - bez credentials: access token idzie w nagłówku Authorization,</li>
 *     <li>{@code /auth/**} - z credentials: przeglądarka musi wysłać/odebrać cookie z refresh tokenem
 *         (frontend wywołuje te endpointy z {@code credentials: 'include'}).</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    static final String API_PATH_PATTERN = "/api/**";
    static final String AUTH_PATH_PATTERN = "/auth/**";
    private static final String WILDCARD = "*";

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        validate(properties);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(API_PATH_PATTERN, build(properties, false));
        source.registerCorsConfiguration(AUTH_PATH_PATTERN, build(properties, true));
        return source;
    }

    private static CorsConfiguration build(CorsProperties properties, boolean allowCredentials) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.allowedOrigins());
        configuration.setAllowedMethods(properties.allowedMethods());
        configuration.setAllowedHeaders(properties.allowedHeaders());
        configuration.setExposedHeaders(properties.exposedHeaders());
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(properties.maxAge());
        return configuration;
    }

    static void validate(CorsProperties properties) {
        if (properties.allowedOrigins().contains(WILDCARD)) {
            throw new IllegalStateException(
                    "crm.security.cors.allowed-origins must list explicit origins - '*' is not allowed "
                            + "because " + AUTH_PATH_PATTERN + " uses credentials (refresh token cookie)");
        }
    }
}
