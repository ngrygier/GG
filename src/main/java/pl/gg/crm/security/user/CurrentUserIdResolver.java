package pl.gg.crm.security.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import pl.gg.crm.security.jwt.JwtProperties;

import java.util.Optional;

/**
 * Ustala identyfikator (Long) zalogowanego pracownika na podstawie {@link Authentication} - z claimu {@code sub} JWT.
 *
 * <p>Fail-closed: dla nieuwierzytelnionego użytkownika, innego typu uwierzytelnienia niż JWT,
 * braku claimu lub wartości niebędącej liczbą zwraca {@link Optional#empty()}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentUserIdResolver {

    private final JwtProperties jwtProperties;

    public Optional<Long> resolveUserId(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        // TODO(LOGIN): jeżeli pojawi się inny mechanizm uwierzytelniania niż JWT, obsłużyć go tutaj.
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return parse(jwtAuthentication.getToken().getClaimAsString(jwtProperties.userIdClaim()));
        }
        return Optional.empty();
    }

    private Optional<Long> parse(String rawUserId) {
        if (rawUserId == null || rawUserId.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.valueOf(rawUserId.trim()));
        } catch (NumberFormatException e) {
            log.warn("JWT claim '{}' is not a numeric employee id", jwtProperties.userIdClaim());
            return Optional.empty();
        }
    }
}
