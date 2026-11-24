
package pl.gg.crm.security.authority;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gg.crm.entity.User;
import pl.gg.crm.repository.UserRepository;
import pl.gg.crm.security.jwt.JwtProperties;
import pl.gg.crm.security.jwt.JwtSigningProperties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long ACCESS_TOKEN_VALIDITY_MINUTES = 45;

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final JwtSigningProperties signingProperties;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public String generateAccessToken(Authentication authentication) {

        // Pobieramy użytkownika na podstawie username z Authentication.
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Authenticated user not found")
                );

        Instant now = Instant.now();
        Instant expiresAt = now.plus(
                ACCESS_TOKEN_VALIDITY_MINUTES,
                ChronoUnit.MINUTES
        );

        // Pobieramy role i permissions z GrantedAuthority.
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .toList();

        List<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .toList();

        // Budujemy claims JWT.
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .audience(List.of(jwtProperties.audience()))
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(jwtProperties.rolesClaim(), roles)
                .claim(jwtProperties.permissionsClaim(), permissions)
                .build();

        // Nagłówek JWT - algorytm RS256 i obowiązkowy kid.
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(signingProperties.keyId())
                .build();

        // Podpisujemy i kodujemy token.
        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
