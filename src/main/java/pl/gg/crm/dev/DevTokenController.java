package pl.gg.crm.dev;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.gg.crm.security.jwt.JwtProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Profile("dev")
@Tag(name = "Dev", description = "Narzędzia deweloperskie")
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevTokenController {

    private static final Duration TOKEN_VALIDITY = Duration.ofHours(8);

    private final DevJwtConfig.DevJwtKeys keys;
    private final JwtProperties properties;

    @Operation(summary = "Pobranie tokenu deweloperskiego JWT")
    @SecurityRequirements
    @GetMapping("/token")
    public DevTokenResponse token(@RequestParam(defaultValue = "1") Integer employeeId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(TOKEN_VALIDITY);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(properties.issuer())
                .audience(properties.audience())
                .subject(String.valueOf(employeeId))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiresAt))
                .claim(properties.rolesClaim(), List.of())
                .claim(properties.permissionsClaim(), List.of())
                .build();
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(keys.keyId())
                .build();
        SignedJWT jwt = new SignedJWT(header, claims);
        try {
            jwt.sign(new RSASSASigner(keys.keyPair().getPrivate()));
        } catch (JOSEException e) {
            throw new IllegalStateException("Cannot sign dev token", e);
        }
        return new DevTokenResponse(jwt.serialize(), "Bearer", expiresAt);
    }

    public record DevTokenResponse(String token, String tokenType, Instant expiresAt) {
    }
}
