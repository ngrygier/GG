
package pl.gg.crm.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableConfigurationProperties(JwtSigningProperties.class)
public class JwtSigningConfig {

    @Bean
    public JwtEncoder jwtEncoder(JwtSigningProperties properties) {

        try (
                InputStream privateInput = properties.privateKey().getInputStream();
                InputStream publicInput = properties.publicKey().getInputStream()
        ) {
            RSAPrivateKey privateKey =
                    RsaKeyConverters.pkcs8().convert(privateInput);

            RSAPublicKey publicKey =
                    RsaKeyConverters.x509().convert(publicInput);

            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(properties.keyId())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .build();

            JWKSource<SecurityContext> jwkSource =
                    new ImmutableJWKSet<>(new JWKSet(rsaKey));

            return new NimbusJwtEncoder(jwkSource);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not load RSA keys for JWT signing", e
            );
        }
    }
}
