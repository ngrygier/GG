package pl.gg.crm.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import pl.gg.crm.security.authority.AuthorityMapper;

import java.util.Collection;
import java.util.List;

/**
 * Mapuje claims ról i uprawnień z JWT na {@link GrantedAuthority}.
 *
 * <p>Fail-closed: brakujący claim lub claim o nieoczekiwanym typie (np. String zamiast listy)
 * nie daje żadnych authorities.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtProperties claims;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return AuthorityMapper.toAuthorities(
                readStringList(jwt, claims.rolesClaim()),
                readStringList(jwt, claims.permissionsClaim()));
    }

    private static List<String> readStringList(Jwt jwt, String claimName) {
        Object value = jwt.getClaims().get(claimName);
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof Collection<?> collection)) {
            log.warn("JWT claim '{}' is not a list - ignoring it", claimName);
            return List.of();
        }
        return collection.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }
}
