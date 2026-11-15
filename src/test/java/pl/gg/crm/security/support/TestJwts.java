package pl.gg.crm.security.support;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import pl.gg.crm.security.jwt.JwtProperties;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Narzędzia testowe: efemeryczne klucze RSA generowane w pamięci przy każdym uruchomieniu testów
 * (nigdy nie zapisywane w repozytorium) i podpisywanie tokenów tak, jak zrobi to moduł logowania.
 */
public final class TestJwts {

    public static final JwtProperties DEFAULT_PROPERTIES =
            new JwtProperties("crm-auth", "crm-api", Map.of(), "roles", "permissions", "sub");

    private TestJwts() {
    }

    public static KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Path writePublicKeyPem(KeyPair keyPair, Path file) {
        String pem = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
                .encodeToString(keyPair.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----\n";
        try {
            return Files.writeString(file, pem, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Claims poprawnego access tokenu wg uzgodnionego kontraktu (iss, aud, sub, 45 min). */
    public static JWTClaimsSet.Builder validClaims() {
        Instant now = Instant.now();
        return new JWTClaimsSet.Builder()
                .issuer("crm-auth")
                .audience("crm-api")
                .subject("7")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(45 * 60)));
    }

    public static JWTClaimsSet.Builder validClaims(List<String> roles, List<String> permissions) {
        return validClaims().claim("roles", roles).claim("permissions", permissions);
    }

    public static String signRs256(KeyPair keyPair, String keyId, JWTClaimsSet claims) {
        JWSHeader.Builder header = new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT);
        if (keyId != null) {
            header.keyID(keyId);
        }
        SignedJWT jwt = new SignedJWT(header.build(), claims);
        try {
            jwt.sign(new RSASSASigner(keyPair.getPrivate()));
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
        return jwt.serialize();
    }

    public static String signHs256(byte[] secret, String keyId, JWTClaimsSet claims) {
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).keyID(keyId).build(), claims);
        try {
            jwt.sign(new MACSigner(secret));
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
        return jwt.serialize();
    }
}
