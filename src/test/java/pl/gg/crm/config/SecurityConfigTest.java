package pl.gg.crm.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.gg.crm.security.jwt.JwtAuthoritiesConverter;
import pl.gg.crm.security.support.TestJwts;
import pl.gg.crm.security.support.SecurityTestConfig;
import pl.gg.crm.security.support.SecurityTestController;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testy produkcyjnego łańcucha filtrów bez skonfigurowanego klucza RSA (stan obecny).
 */
@WebMvcTest(controllers = SecurityTestController.class)
@Import(SecurityTestConfig.class)
class SecurityConfigTest {

    private static final JwtAuthoritiesConverter AUTHORITIES =
            new JwtAuthoritiesConverter(TestJwts.DEFAULT_PROPERTIES);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private List<SecurityFilterChain> filterChains;

    @Autowired
    private ObjectProvider<JwtDecoder> jwtDecoder;

    @Test
    void passwordEncoderIsBcrypt() {
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
        String hash = passwordEncoder.encode("secret");
        assertThat(hash).isNotEqualTo("secret");
        assertThat(passwordEncoder.matches("secret", hash)).isTrue();
        assertThat(passwordEncoder.matches("other", hash)).isFalse();
    }

    @Test
    void exactlyOneFilterChainAndNoJwtDecoderWithoutRsaKey() {
        assertThat(filterChains).hasSize(1);
        assertThat(jwtDecoder.getIfAvailable()).isNull();
    }

    @Test
    void publicEndpointIsAccessibleAnonymously() throws Exception {
        mvc.perform(get("/api/public/ping")).andExpect(status().isOk());
    }

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/secure/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")));
    }

    @Test
    void unknownEndpointsRequireAuthentication() throws Exception {
        mvc.perform(get("/api/not-mapped")).andExpect(status().isUnauthorized());
        mvc.perform(get("/")).andExpect(status().isUnauthorized());
    }

    @Test
    void bearerTokenIsRejectedWhileJwtIsNotConfigured() throws Exception {
        mvc.perform(get("/api/secure/ping").header(HttpHeaders.AUTHORIZATION, "Bearer some.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginEndpointIsPublicButOtherAuthEndpointsAreNot() throws Exception {
        mvc.perform(post("/auth/login")).andExpect(status().isOk());
        mvc.perform(post("/auth/refresh")).andExpect(status().isUnauthorized());
    }

    @Test
    void authEndpointsAllowCredentialedCorsForRefreshCookie() throws Exception {
        mvc.perform(options("/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void apiEndpointsDoNotAllowCredentialedCors() throws Exception {
        mvc.perform(options("/api/secure/ping")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void httpBasicIsNotAccepted() throws Exception {
        mvc.perform(get("/api/secure/ping").header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNzd29yZA=="))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void authenticatedUserCanAccessProtectedEndpointWithoutSession() throws Exception {
        MvcResult result = mvc.perform(get("/api/secure/ping")).andExpect(status().isOk()).andReturn();
        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    @WithMockUser(authorities = "CLIENT_CREATE")
    void preAuthorizeGrantsAccessWithRequiredPermission() throws Exception {
        // POST bez tokenu CSRF - CSRF jest wyłączony dla bezstanowego API z tokenem w nagłówku.
        mvc.perform(post("/api/clients")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "CLIENT_VIEW")
    void preAuthorizeDeniesAccessWithoutRequiredPermission() throws Exception {
        mvc.perform(post("/api/clients")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void roleAloneDoesNotGrantPermission() throws Exception {
        mvc.perform(post("/api/clients")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "TASK_VIEW")
    void recordAccessIsDeniedWhenAssignmentCannotBeVerified() throws Exception {
        mvc.perform(get("/api/tasks/1")).andExpect(status().isForbidden());
    }

    @Test
    void recordAccessIsDeniedForJwtUserUntilDatabaseIntegrationExists() throws Exception {
        mvc.perform(get("/api/tasks/1").with(jwt()
                        .jwt(token -> token.subject("42").claim("permissions", List.of("TASK_VIEW")))
                        .authorities(AUTHORITIES)))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewAllPermissionGrantsRecordAccess() throws Exception {
        mvc.perform(get("/api/tasks/1").with(jwt()
                        .jwt(token -> token.subject("1").claim("permissions", List.of("TASK_VIEW", "TASK_VIEW_ALL")))
                        .authorities(AUTHORITIES)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "TASK_VIEW_ALL")
    void viewAllPermissionDoesNotReplaceBasePermission() throws Exception {
        mvc.perform(get("/api/tasks/1")).andExpect(status().isForbidden());
    }

    @Test
    void corsPreflightIsAllowedForConfiguredOrigin() throws Exception {
        mvc.perform(options("/api/secure/ping")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    @Test
    void corsPreflightIsRejectedForUnknownOrigin() throws Exception {
        mvc.perform(options("/api/secure/ping")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}
