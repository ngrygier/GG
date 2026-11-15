package pl.gg.crm.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import pl.gg.crm.security.support.TestJwts;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthoritiesConverterTest {

    private final JwtAuthoritiesConverter converter =
            new JwtAuthoritiesConverter(TestJwts.DEFAULT_PROPERTIES);

    @Test
    void mapsRolesWithPrefixAndPermissionsAsIs() {
        Jwt jwt = jwt(Map.of(
                "roles", List.of("OWNER"),
                "permissions", List.of("CLIENT_CREATE", "TASK_VIEW_ALL")));

        assertThat(names(converter.convert(jwt)))
                .containsExactlyInAnyOrder("ROLE_OWNER", "CLIENT_CREATE", "TASK_VIEW_ALL");
    }

    @Test
    void acceptsPermissionsNotKnownInCode() {
        Jwt jwt = jwt(Map.of("permissions", List.of("INVOICE_EXPORT")));

        assertThat(names(converter.convert(jwt))).containsExactly("INVOICE_EXPORT");
    }

    @Test
    void missingClaimsGiveNoAuthorities() {
        assertThat(converter.convert(jwt(Map.of()))).isEmpty();
    }

    @Test
    void claimOfUnexpectedTypeIsIgnored() {
        Jwt jwt = jwt(Map.of("roles", "OWNER", "permissions", "CLIENT_CREATE"));

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    void blankAndNonStringEntriesAreIgnored() {
        Jwt jwt = jwt(Map.of("permissions", Arrays.asList(" ", 42, " CLIENT_VIEW ")));

        assertThat(names(converter.convert(jwt))).containsExactly("CLIENT_VIEW");
    }

    @Test
    void permissionCannotImpersonateRole() {
        Jwt jwt = jwt(Map.of("permissions", List.of("ROLE_OWNER", "CLIENT_VIEW")));

        assertThat(names(converter.convert(jwt))).containsExactly("CLIENT_VIEW");
    }

    @Test
    void roleAlreadyPrefixedIsNotDoublePrefixed() {
        Jwt jwt = jwt(Map.of("roles", List.of("ROLE_OWNER")));

        assertThat(names(converter.convert(jwt))).containsExactly("ROLE_OWNER");
    }

    @Test
    void claimNamesAreConfigurable() {
        JwtAuthoritiesConverter custom =
                new JwtAuthoritiesConverter(new JwtProperties("crm-auth", "crm-api", Map.of(), "crm_roles", "crm_perms", "uid"));
        Jwt jwt = jwt(Map.of(
                "crm_roles", List.of("PROJECT_WORKER"),
                "crm_perms", List.of("PROJECT_VIEW"),
                "permissions", List.of("CLIENT_DELETE")));

        assertThat(names(custom.convert(jwt))).containsExactlyInAnyOrder("ROLE_PROJECT_WORKER", "PROJECT_VIEW");
    }

    @Test
    void authenticationConverterUsesConfiguredUserIdClaimAsName() {
        JwtAuthenticationConverter authenticationConverter = new JwtSecurityConfig()
                .jwtAuthenticationConverter(new JwtProperties("crm-auth", "crm-api", Map.of(), "roles", "permissions", "employee_id"));
        Jwt jwt = jwt(Map.of("employee_id", "17", "permissions", List.of("TASK_VIEW")));

        var authentication = authenticationConverter.convert(jwt);

        assertThat(authentication.getName()).isEqualTo("17");
        // Spring Security 7 dodaje też authority FACTOR_BEARER (informacja o sposobie uwierzytelnienia).
        assertThat(names(authentication.getAuthorities())).contains("TASK_VIEW").noneMatch(a -> a.startsWith("ROLE_"));
    }

    private static Jwt jwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("subject")
                .claims(c -> c.putAll(claims))
                .build();
    }

    private static List<String> names(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().map(GrantedAuthority::getAuthority).toList();
    }
}
