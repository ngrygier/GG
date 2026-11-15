package pl.gg.crm.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Weryfikacja JWT RS256 (Spring Security jako Resource Server). Ten moduł zna wyłącznie klucze PUBLICZNE -
 * podpisywanie tokenów (klucz prywatny) należy do modułu logowania i powinno żyć poza tym pakietem.
 *
 * <ul>
 *     <li>{@link JwtDecoder} - tworzony tylko, gdy skonfigurowano co najmniej jeden klucz publiczny
 *         ({@value JwtProperties#PUBLIC_KEYS_PROPERTY}); {@code SecurityConfig} wtedy włącza uwierzytelnianie JWT,</li>
 *     <li>{@link JwtAuthenticationConverter} - zamienia zweryfikowany JWT na authorities ({@link JwtAuthoritiesConverter}).</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtSecurityConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(JwtProperties properties) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtAuthoritiesConverter(properties));
        converter.setPrincipalClaimName(properties.userIdClaim());
        return converter;
    }

    /**
     * Akceptuje wyłącznie tokeny, które:
     * <ul>
     *     <li>są podpisane RS256 kluczem wskazanym przez nagłówek {@code kid} (token bez {@code kid} lub z nieznanym {@code kid} jest odrzucany),</li>
     *     <li>mają {@code iss} i {@code aud} zgodne z konfiguracją,</li>
     *     <li>mają {@code exp} (i nie są przeterminowane) oraz claim identyfikujący pracownika.</li>
     * </ul>
     */
    @Bean
    @Conditional(PublicKeysConfigured.class)
    public JwtDecoder jwtDecoder(JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSource(kidRequiredJwkSource(properties.publicKeys()))
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(
                new JwtIssuerValidator(properties.issuer()),
                new JwtAudienceValidator(properties.audience()),
                new JwtClaimValidator<>(JwtClaimNames.EXP, Objects::nonNull),
                new JwtClaimValidator<>(properties.userIdClaim(), Objects::nonNull)));
        return decoder;
    }

    private static JWKSource<SecurityContext> kidRequiredJwkSource(Map<String, Resource> publicKeys) {
        List<JWK> keys = publicKeys.entrySet().stream()
                .map(entry -> toJwk(entry.getKey(), entry.getValue()))
                .toList();
        JWKSet keySet = new JWKSet(keys);
        return (selector, context) -> {
            Set<String> requestedKeyIds = selector.getMatcher().getKeyIDs();
            if (requestedKeyIds == null || requestedKeyIds.isEmpty() || requestedKeyIds.contains(null)) {
                return List.of(); // brak kid w nagłówku -> brak klucza -> token odrzucony
            }
            return selector.select(keySet);
        };
    }

    private static JWK toJwk(String keyId, Resource location) {
        try (InputStream input = location.getInputStream()) {
            RSAPublicKey publicKey = RsaKeyConverters.x509().convert(input);
            return new RSAKey.Builder(publicKey)
                    .keyID(keyId)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .build();
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Cannot read RSA public key '" + keyId + "' from " + location, e);
        }
    }

    static class PublicKeysConfigured extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            boolean configured = Binder.get(context.getEnvironment())
                    .bind(JwtProperties.PUBLIC_KEYS_PROPERTY, Bindable.mapOf(String.class, String.class))
                    .map(keys -> !keys.isEmpty())
                    .orElse(false);
            return configured
                    ? ConditionOutcome.match("JWT public keys configured")
                    : ConditionOutcome.noMatch("No JWT public keys configured (" + JwtProperties.PUBLIC_KEYS_PROPERTY + ")");
        }
    }
}
