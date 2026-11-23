
package pl.gg.crm.security.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("crm.security.jwt.signing")
public record JwtSigningProperties(
        @NotNull Resource privateKey,
        @NotNull Resource publicKey,
        @NotBlank String keyId
) {
}
