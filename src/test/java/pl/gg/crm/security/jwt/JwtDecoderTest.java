package pl.gg.crm.security.jwt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import pl.gg.crm.security.support.TestJwts;

import java.nio.file.Path;
import java.security.KeyPair;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Weryfikacja prawdziwych tokenów RS256 przez {@link JwtSecurityConfig#jwtDecoder(JwtProperties)}.
 */
class JwtDecoderTest {

    private static final String OLD_KID = "2026-10";
    private static final String CURRENT_KID = "2026-11";

    private static KeyPair oldKey;
    private static KeyPair currentKey;
    private static KeyPair unknownKey;
    private static JwtDecoder decoder;

    @BeforeAll
    static void setUp(@TempDir Path dir) {
        oldKey = TestJwts.generateRsaKeyPair();
        currentKey = TestJwts.generateRsaKeyPair();
        unknownKey = TestJwts.generateRsaKeyPair();

        Map<String, Resource> publicKeys = new LinkedHashMap<>();
        publicKeys.put(OLD_KID, new FileSystemResource(TestJwts.writePublicKeyPem(oldKey, dir.resolve("old.pem"))));
        publicKeys.put(CURRENT_KID, new FileSystemResource(TestJwts.writePublicKeyPem(currentKey, dir.resolve("current.pem"))));

        decoder = new JwtSecurityConfig().jwtDecoder(
                new JwtProperties("crm-auth", "crm-api", publicKeys, "roles", "permissions", "sub"));
    }

    @Test
    void acceptsValidTokenSignedWithCurrentKey() {
        Jwt jwt = decoder.decode(TestJwts.signRs256(currentKey, CURRENT_KID, TestJwts.validClaims().build()));

        assertThat(jwt.getSubject()).isEqualTo("7");
        // iss "crm-auth" nie jest URL-em, więc Jwt#getIssuer() rzuca wyjątek - czytać jako String.
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("crm-auth");
        assertThat(jwt.getAudience()).containsExactly("crm-api");
    }

    @Test
    void acceptsTokenSignedWithOlderConfiguredKey() {
        assertThat(decoder.decode(TestJwts.signRs256(oldKey, OLD_KID, TestJwts.validClaims().build())))
                .isNotNull();
    }

    @Test
    void rejectsTokenWithoutKid() {
        assertRejected(TestJwts.signRs256(currentKey, null, TestJwts.validClaims().build()));
    }

    @Test
    void rejectsTokenWithUnknownKid() {
        assertRejected(TestJwts.signRs256(unknownKey, "unknown", TestJwts.validClaims().build()));
    }

    @Test
    void rejectsTokenSignedWithDifferentKeyThanKidPointsTo() {
        assertRejected(TestJwts.signRs256(oldKey, CURRENT_KID, TestJwts.validClaims().build()));
        assertRejected(TestJwts.signRs256(unknownKey, CURRENT_KID, TestJwts.validClaims().build()));
    }

    @Test
    void rejectsTamperedPayload() {
        String token = TestJwts.signRs256(currentKey, CURRENT_KID, TestJwts.validClaims().build());
        String[] parts = token.split("\\.");
        String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                TestJwts.validClaims().subject("1").build().toString().getBytes());

        assertRejected(parts[0] + "." + forgedPayload + "." + parts[2]);
    }

    @Test
    void rejectsSymmetricAlgorithm() {
        byte[] secret = new byte[32];
        Arrays.fill(secret, (byte) 1);

        assertRejected(TestJwts.signHs256(secret, CURRENT_KID, TestJwts.validClaims().build()));
    }

    @Test
    void rejectsWrongIssuer() {
        assertRejected(TestJwts.signRs256(currentKey, CURRENT_KID, TestJwts.validClaims().issuer("other").build()));
    }

    @Test
    void rejectsWrongAudience() {
        assertRejected(TestJwts.signRs256(currentKey, CURRENT_KID, TestJwts.validClaims().audience("other-api").build()));
    }

    @Test
    void rejectsTokenWithoutExpiration() {
        assertRejected(TestJwts.signRs256(currentKey, CURRENT_KID, TestJwts.validClaims().expirationTime(null).build()));
    }

    @Test
    void rejectsExpiredToken() {
        Instant past = Instant.now().minusSeconds(3600);
        assertRejected(TestJwts.signRs256(currentKey, CURRENT_KID, TestJwts.validClaims()
                .issueTime(Date.from(past.minusSeconds(60)))
                .expirationTime(Date.from(past))
                .build()));
    }

    @Test
    void rejectsTokenWithoutSubject() {
        assertRejected(TestJwts.signRs256(currentKey, CURRENT_KID, TestJwts.validClaims().subject(null).build()));
    }

    private static void assertRejected(String token) {
        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }
}
