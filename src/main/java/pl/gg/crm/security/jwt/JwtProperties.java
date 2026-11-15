package pl.gg.crm.security.jwt;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Kontrakt JWT między modułem logowania (wystawia tokeny) a tym Resource Serverem (weryfikuje tokeny).
 * Jedyne miejsce w kodzie z założeniami o tokenie; wszystko można nadpisać przez {@code crm.security.jwt.*}.
 *
 * <p>Uzgodnione (2026-11):
 * <ul>
 *     <li>{@code iss} = {@code crm-auth}, {@code aud} = {@code crm-api},</li>
 *     <li>podpis RS256, nagłówek {@code kid} obowiązkowy,</li>
 *     <li>{@code sub} = identyfikator pracownika (Long zapisany jako String),</li>
 *     <li>access token ważny ok. 45 minut.</li>
 * </ul>
 * TODO(JWT): nazwy claimów {@code roles} i {@code permissions} są wciąż robocze - potwierdzić z modułem logowania.
 *
 * <p>Uwaga: JWT jest podpisany, nie szyfrowany - jego payload jest czytelny dla każdego. Nie umieszczać w nim danych wrażliwych.
 *
 * @param publicKeys klucze publiczne RSA (PEM, {@code -----BEGIN PUBLIC KEY-----}) zmapowane po {@code kid}, np.
 *                   {@code crm.security.jwt.public-keys[2026-11]=file:/run/secrets/jwt-2026-11.pub.pem}.
 *                   Kilka wpisów = rotacja kluczy (stare tokeny dalej przechodzą weryfikację). Pusta mapa = JWT wyłączony.
 */
@Validated
@ConfigurationProperties("crm.security.jwt")
public record JwtProperties(
        @DefaultValue("crm-auth") @NotBlank String issuer,
        @DefaultValue("crm-api") @NotBlank String audience,
        @DefaultValue Map<String, Resource> publicKeys,
        @DefaultValue("roles") @NotBlank String rolesClaim,
        @DefaultValue("permissions") @NotBlank String permissionsClaim,
        @DefaultValue("sub") @NotBlank String userIdClaim
) {

    public static final String PUBLIC_KEYS_PROPERTY = "crm.security.jwt.public-keys";
}
