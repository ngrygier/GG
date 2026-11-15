package pl.gg.crm.security.authority;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Jedyne miejsce, które zamienia nazwy ról i uprawnień (Stringi z bazy danych lub z JWT) na {@link GrantedAuthority}.
 *
 * <ul>
 *     <li>rola {@code OWNER} -> authority {@code ROLE_OWNER} (do użycia z {@code hasRole('OWNER')}),</li>
 *     <li>uprawnienie {@code CLIENT_CREATE} -> authority {@code CLIENT_CREATE} (do użycia z {@code hasAuthority('CLIENT_CREATE')}).</li>
 * </ul>
 *
 * <p>Nazwy nie są tu zdefiniowane ani walidowane względem żadnej listy - pochodzą dynamicznie z bazy.
 * Może z tego korzystać również przyszła implementacja logowania (np. przy budowie {@code UserDetails}).
 */
@Slf4j
public final class AuthorityMapper {

    public static final String ROLE_PREFIX = "ROLE_";

    private AuthorityMapper() {
    }

    public static Set<GrantedAuthority> toAuthorities(Collection<String> roles, Collection<String> permissions) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        if (roles != null) {
            roles.stream()
                    .map(AuthorityMapper::normalize)
                    .filter(name -> !name.isEmpty())
                    .map(name -> name.startsWith(ROLE_PREFIX) ? name : ROLE_PREFIX + name)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }
        if (permissions != null) {
            permissions.stream()
                    .map(AuthorityMapper::normalize)
                    .filter(name -> !name.isEmpty())
                    .filter(AuthorityMapper::isNotRoleLike)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }
        return Collections.unmodifiableSet(authorities);
    }

    private static String normalize(String name) {
        return name == null ? "" : name.trim();
    }

    /**
     * Role i uprawnienia są rozdzielone: uprawnienie nie może "udawać" roli przez prefiks ROLE_.
     */
    private static boolean isNotRoleLike(String permission) {
        if (permission.startsWith(ROLE_PREFIX)) {
            log.warn("Ignoring permission '{}' - permissions must not use the '{}' prefix", permission, ROLE_PREFIX);
            return false;
        }
        return true;
    }
}
