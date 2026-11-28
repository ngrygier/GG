package pl.gg.crm.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import pl.gg.crm.security.support.SecurityTestConfig;
import pl.gg.testsupport.SecurityTestController;
import pl.gg.crm.security.support.TestJwts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pełna ścieżka: konfiguracja klucza publicznego w properties -> {@link JwtDecoder} -> włączony Resource Server ->
 * mapowanie claims na authorities -> {@code @PreAuthorize}. Tokeny są podpisywane efemerycznym kluczem RSA.
 */
@WebMvcTest(controllers = SecurityTestController.class)
@Import({SecurityTestConfig.class, SecurityTestController.class})
class JwtResourceServerTest {

    private static final String KID = "test-key";
    private static final KeyPair KEY = TestJwts.generateRsaKeyPair();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    @DynamicPropertySource
    static void jwtPublicKey(DynamicPropertyRegistry registry) throws IOException {
        Path pem = Files.createTempFile("crm-jwt-test", ".pem");
        pem.toFile().deleteOnExit();
        TestJwts.writePublicKeyPem(KEY, pem);
        registry.add(JwtProperties.PUBLIC_KEYS_PROPERTY + "[" + KID + "]", () -> pem.toUri().toString());
    }

    @Test
    void decoderIsCreatedFromConfiguredPublicKey() {
        assertThat(jwtDecoder).isNotNull();
    }

    @Test
    void validTokenWithPermissionGrantsAccess() throws Exception {
        mvc.perform(post("/api/clients").header(HttpHeaders.AUTHORIZATION,
                        bearer(List.of("OFFICE_WORKER"), List.of("CLIENT_VIEW", "CLIENT_CREATE"))))
                .andExpect(status().isOk());
    }

    @Test
    void validTokenWithoutPermissionIsForbidden() throws Exception {
        String token = bearer(List.of(), List.of());
        mvc.perform(get("/api/secure/ping").header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isOk());
        mvc.perform(post("/api/clients").header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isForbidden());
    }

    @Test
    void permissionNameInRolesClaimIsNotTreatedAsPermission() throws Exception {
        mvc.perform(post("/api/clients").header(HttpHeaders.AUTHORIZATION, bearer(List.of("CLIENT_CREATE"), List.of())))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewAllPermissionGrantsRecordAccessButAssignmentIsNotYetChecked() throws Exception {
        mvc.perform(get("/api/tasks/1").header(HttpHeaders.AUTHORIZATION,
                        bearer(List.of("OWNER"), List.of("TASK_VIEW", "TASK_VIEW_ALL"))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/tasks/1").header(HttpHeaders.AUTHORIZATION,
                        bearer(List.of("PROJECT_WORKER"), List.of("TASK_VIEW"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenSignedWithOtherKeyIsUnauthorized() throws Exception {
        String forged = TestJwts.signRs256(TestJwts.generateRsaKeyPair(), KID,
                TestJwts.validClaims(List.of("OWNER"), List.of("CLIENT_CREATE")).build());

        mvc.perform(post("/api/clients").header(HttpHeaders.AUTHORIZATION, "Bearer " + forged))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedOrMissingTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/secure/ping").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/secure/ping")).andExpect(status().isUnauthorized());
    }

    private static String bearer(List<String> roles, List<String> permissions) {
        return "Bearer " + TestJwts.signRs256(KEY, KID, TestJwts.validClaims(roles, permissions).build());
    }
}
