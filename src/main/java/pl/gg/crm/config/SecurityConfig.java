package pl.gg.crm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Główna konfiguracja Spring Security dla bezstanowego REST API.
 *
 * <p>Uwierzytelnianie odbywa się wyłącznie przez token Bearer (JWT RS256) w nagłówku
 * {@code Authorization}. Dopóki w kontekście nie ma beana {@link JwtDecoder}
 * (brak skonfigurowanego klucza publicznego), Resource Server nie jest włączany,
 * a wszystkie niepubliczne endpointy odpowiadają 401 (fail-closed).
 */
@Slf4j
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/public/**",
            // TODO(LOGIN): kolejne endpointy modułu logowania (np. odświeżanie tokenu przez cookie) dopisywać tutaj.
            "/auth/login",
            // Strona błędów Spring Boot - bez tego wyjątek na publicznym endpoincie kończyłby się 401 zamiast właściwym statusem.
            "/error"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ObjectProvider<JwtDecoder> jwtDecoderProvider,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .cors(Customizer.withDefaults())
                // CSRF wyłączony świadomie: API jest bezstanowe, access token jest wysyłany jawnie w nagłówku
                // Authorization (przeglądarka nie dołącza go automatycznie jak cookie), więc atak CSRF nie ma
                // czego wykorzystać. Wyjątek: refresh token w cookie (/auth/**) - chroni go SameSite=Strict,
                // CORS z listą originów i wymagania opisane w SECURITY.md.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated());

        configureJwtResourceServer(http, jwtDecoderProvider.getIfAvailable(), jwtAuthenticationConverter);
        return http.build();
    }

    /**
     * Punkt rozszerzenia JWT. Resource Server jest włączany tylko wtedy, gdy istnieje {@link JwtDecoder} -
     * {@link pl.gg.crm.security.jwt.JwtSecurityConfig} tworzy go po skonfigurowaniu kluczy publicznych
     * ({@code crm.security.jwt.public-keys[...]}).
     */
    private void configureJwtResourceServer(HttpSecurity http,
                                            JwtDecoder jwtDecoder,
                                            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        if (jwtDecoder == null) {
            // TODO(JWT): usunąć tę gałąź, gdy klucz publiczny RSA będzie zawsze konfigurowany we wszystkich środowiskach.
            log.warn("No JwtDecoder configured (crm.security.jwt.public-keys is empty) - JWT authentication is disabled, "
                    + "every non-public endpoint will respond with 401.");
            return;
        }
        http.oauth2ResourceServer(oauth2 -> oauth2
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                .jwt(jwt -> jwt
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter)));
    }
}
