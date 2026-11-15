package pl.gg.crm.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class CorsConfigTest {

    private final CorsConfig corsConfig = new CorsConfig();

    @Test
    void wildcardOriginIsRejected() {
        CorsProperties properties = properties(List.of("http://localhost:5173", "*"));

        assertThatIllegalStateException().isThrownBy(() -> corsConfig.corsConfigurationSource(properties));
    }

    @Test
    void apiPathsDoNotUseCredentialsAndAuthPathsDo() {
        var source = corsConfig.corsConfigurationSource(properties(List.of("http://localhost:5173")));

        CorsConfiguration api = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/clients"));
        CorsConfiguration auth = source.getCorsConfiguration(new MockHttpServletRequest("POST", "/auth/login"));
        CorsConfiguration other = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/other"));

        assertThat(api).isNotNull();
        assertThat(api.getAllowedOrigins()).containsExactly("http://localhost:5173");
        assertThat(api.getAllowedMethods()).doesNotContain("*");
        assertThat(api.getAllowCredentials()).isFalse();
        assertThat(auth).isNotNull();
        assertThat(auth.getAllowedOrigins()).containsExactly("http://localhost:5173");
        assertThat(auth.getAllowCredentials()).isTrue();
        assertThat(other).isNull();
    }

    private static CorsProperties properties(List<String> origins) {
        return new CorsProperties(origins, List.of("GET", "POST"), List.of("Authorization", "Content-Type"),
                List.of("Location"), Duration.ofHours(1));
    }
}
