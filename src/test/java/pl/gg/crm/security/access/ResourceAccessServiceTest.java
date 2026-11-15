package pl.gg.crm.security.access;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import pl.gg.crm.security.support.TestJwts;
import pl.gg.crm.security.user.CurrentUserIdResolver;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ResourceAccessServiceTest {

    private static final ResourceAccessProperties PROPERTIES =
            new ResourceAccessProperties("PROJECT_VIEW_ALL", "TASK_VIEW_ALL", "CLIENT_VIEW_ALL", "DESIGNER_VIEW_ALL");
    private static final CurrentUserIdResolver USER_ID_RESOLVER =
            new CurrentUserIdResolver(TestJwts.DEFAULT_PROPERTIES);

    private final ResourceAssignmentLookup lookup = mock(ResourceAssignmentLookup.class);
    private final ResourceAccessService service = new ResourceAccessService(lookup, USER_ID_RESOLVER, PROPERTIES);

    @Test
    void deniesMissingOrAnonymousAuthentication() {
        Authentication anonymous = new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        assertThat(service.canAccessTask(null, 1L)).isFalse();
        assertThat(service.canAccessTask(anonymous, 1L)).isFalse();
        verifyNoInteractions(lookup);
    }

    @Test
    void deniesUnauthenticatedTokenEvenWithViewAllAuthority() {
        TestingAuthenticationToken token = new TestingAuthenticationToken("user", null, "TASK_VIEW_ALL");
        token.setAuthenticated(false);

        assertThat(service.canAccessTask(token, 1L)).isFalse();
    }

    @Test
    void deniesMissingResourceId() {
        assertThat(service.canAccessTask(jwtUser("7", "TASK_VIEW_ALL"), null)).isFalse();
    }

    @Test
    void viewAllAuthorityGrantsAccessWithoutLookup() {
        assertThat(service.canAccessTask(jwtUser("7", "TASK_VIEW_ALL"), 1L)).isTrue();
        assertThat(service.canAccessProject(jwtUser("7", "PROJECT_VIEW_ALL"), 1L)).isTrue();
        assertThat(service.canAccessClient(jwtUser("7", "CLIENT_VIEW_ALL"), 1L)).isTrue();
        assertThat(service.canAccessDesigner(jwtUser("7", "DESIGNER_VIEW_ALL"), 1L)).isTrue();
        verifyNoInteractions(lookup);
    }

    @Test
    void viewAllAuthorityOfOtherResourceDoesNotGrantAccess() {
        assertThat(service.canAccessClient(jwtUser("7", "TASK_VIEW_ALL"), 1L)).isFalse();
    }

    @Test
    void delegatesToAssignmentLookupWithUserIdFromJwt() {
        when(lookup.isTaskAssignedTo(7L, 1L)).thenReturn(true);
        when(lookup.isClientInAssignedProject(7L, 5L)).thenReturn(true);

        assertThat(service.canAccessTask(jwtUser("7", "TASK_VIEW"), 1L)).isTrue();
        assertThat(service.canAccessTask(jwtUser("8", "TASK_VIEW"), 1L)).isFalse();
        assertThat(service.canAccessClient(jwtUser("7", "CLIENT_VIEW"), 5L)).isTrue();
        assertThat(service.canAccessDesigner(jwtUser("7", "DESIGNER_VIEW"), 5L)).isFalse();
    }

    @Test
    void deniesWhenUserIdCannotBeResolved() {
        when(lookup.isTaskAssignedTo(any(), any())).thenReturn(true);

        Authentication nonJwt = new TestingAuthenticationToken("user", null, "TASK_VIEW");
        Authentication jwtWithoutSubject = new JwtAuthenticationToken(Jwt.withTokenValue("t")
                .header("alg", "RS256").claim("other", "x").build(),
                AuthorityUtils.createAuthorityList("TASK_VIEW"));

        assertThat(service.canAccessTask(nonJwt, 1L)).isFalse();
        assertThat(service.canAccessTask(jwtWithoutSubject, 1L)).isFalse();
        assertThat(service.canAccessTask(jwtUser("not-a-number", "TASK_VIEW"), 1L)).isFalse();
        verifyNoInteractions(lookup);
    }

    @Test
    void blankViewAllConfigurationGrantsNothing() {
        ResourceAccessService noViewAll = new ResourceAccessService(lookup, USER_ID_RESOLVER,
                new ResourceAccessProperties("", "", null, " "));

        assertThat(noViewAll.canAccessTask(jwtUser("7", "TASK_VIEW_ALL"), 1L)).isFalse();
        assertThat(noViewAll.canAccessClient(jwtUser("7", "CLIENT_VIEW_ALL"), 1L)).isFalse();
        assertThat(noViewAll.canAccessDesigner(jwtUser("7", "DESIGNER_VIEW_ALL"), 1L)).isFalse();
    }

    @Test
    void temporaryLookupIsFailClosed() {
        ResourceAccessService withTemporaryLookup = new ResourceAccessService(
                new NotImplementedResourceAssignmentLookup(), USER_ID_RESOLVER, PROPERTIES);
        Authentication user = jwtUser("7", "TASK_VIEW", "PROJECT_VIEW", "CLIENT_VIEW", "DESIGNER_VIEW");

        assertThat(withTemporaryLookup.canAccessTask(user, 1L)).isFalse();
        assertThat(withTemporaryLookup.canAccessProject(user, 1L)).isFalse();
        assertThat(withTemporaryLookup.canAccessClient(user, 1L)).isFalse();
        assertThat(withTemporaryLookup.canAccessDesigner(user, 1L)).isFalse();
    }

    private static Authentication jwtUser(String userId, String... authorities) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "RS256").subject(userId).build();
        return new JwtAuthenticationToken(jwt, AuthorityUtils.createAuthorityList(authorities));
    }
}
