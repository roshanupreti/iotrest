package com.project.iotrest.access.providers;

import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.security.JWTUserPrincipal;
import com.project.iotrest.service.token.JWTProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    @Mock
    private JWTProvider jwtProvider;

    @Mock
    private ContainerRequestContext requestContext;

    private AuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AuthenticationFilter(jwtProvider);
    }

    private static Claims claimsFor(String subject, Integer id, String email, List<String> accessLevels) {
        Claims claims = Jwts.claims();
        claims.setSubject(subject);
        claims.put("id", id);
        claims.put("email", email);
        claims.put("access-levels", accessLevels);
        return claims;
    }

    @Test
    void filter_missingAuthorizationHeader_throwsUnauthorized() {
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        assertThatThrownBy(() -> filter.filter(requestContext))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> {
                    assertThat(((RESTException) e).getStatusCode()).isEqualTo(401);
                    assertThat(((RESTException) e).getResponseMessage()).isEqualTo("Missing Authorization Header.");
                });
    }

    @Test
    void filter_headerWithoutBearerScheme_throwsUnauthorized() {
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Basic abc123");

        assertThatThrownBy(() -> filter.filter(requestContext))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getResponseMessage()).isEqualTo("Missing Authorization Header."));
    }

    @Test
    void filter_jwtProviderReturnsNullClaims_throwsInvalidToken() {
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer sometoken");
        when(jwtProvider.parseAndValidateToken("sometoken")).thenReturn(null);

        assertThatThrownBy(() -> filter.filter(requestContext))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> {
                    assertThat(((RESTException) e).getStatusCode()).isEqualTo(401);
                    assertThat(((RESTException) e).getResponseMessage()).isEqualTo("Invalid token");
                });
    }

    @Test
    void filter_trimsBearerPrefixBeforeParsing() {
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer   abc.def.ghi");
        when(jwtProvider.parseAndValidateToken("abc.def.ghi"))
                .thenReturn(claimsFor("john", 1, "john@example.org", List.of("READ")));
        when(requestContext.getSecurityContext()).thenReturn(fakeCurrentSecurityContext(true));

        filter.filter(requestContext);

        verify(jwtProvider).parseAndValidateToken("abc.def.ghi");
    }

    @Test
    void filter_validToken_setsSecurityContextWithMatchingPrincipalAndRoles() {
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid.jwt.token");
        when(jwtProvider.parseAndValidateToken("valid.jwt.token"))
                .thenReturn(claimsFor("john", 7, "john@example.org", List.of("READ", "CREATE")));
        when(requestContext.getSecurityContext()).thenReturn(fakeCurrentSecurityContext(true));

        filter.filter(requestContext);

        ArgumentCaptor<SecurityContext> captor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(requestContext).setSecurityContext(captor.capture());
        SecurityContext newContext = captor.getValue();

        assertThat(newContext.getUserPrincipal()).isInstanceOf(JWTUserPrincipal.class);
        JWTUserPrincipal principal = (JWTUserPrincipal) newContext.getUserPrincipal();
        assertThat(principal.getId()).isEqualTo(7);
        assertThat(principal.getName()).isEqualTo("john");
        assertThat(principal.getEmail()).isEqualTo("john@example.org");

        assertThat(newContext.isUserInRole("READ")).isTrue();
        assertThat(newContext.isUserInRole("CREATE")).isTrue();
        assertThat(newContext.isUserInRole("DELETE")).isFalse();
        assertThat(newContext.getAuthenticationScheme()).isEqualTo("Bearer");
        assertThat(newContext.isSecure()).isTrue();
    }

    @Test
    void filter_isSecureDelegatesToPreviousSecurityContext() {
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid.jwt.token");
        when(jwtProvider.parseAndValidateToken("valid.jwt.token"))
                .thenReturn(claimsFor("john", 7, "john@example.org", List.of("READ")));
        when(requestContext.getSecurityContext()).thenReturn(fakeCurrentSecurityContext(false));

        filter.filter(requestContext);

        ArgumentCaptor<SecurityContext> captor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(requestContext).setSecurityContext(captor.capture());
        assertThat(captor.getValue().isSecure()).isFalse();
    }

    @Test
    void filter_missingAccessLevelsClaim_defaultsToNoAccessInsteadOfThrowing() {
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid.jwt.token");
        Claims claimsWithoutAccessLevels = Jwts.claims();
        claimsWithoutAccessLevels.setSubject("john");
        claimsWithoutAccessLevels.put("id", 1);
        claimsWithoutAccessLevels.put("email", "john@example.org");
        when(jwtProvider.parseAndValidateToken("valid.jwt.token")).thenReturn(claimsWithoutAccessLevels);
        when(requestContext.getSecurityContext()).thenReturn(fakeCurrentSecurityContext(true));

        filter.filter(requestContext);

        ArgumentCaptor<SecurityContext> captor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(requestContext).setSecurityContext(captor.capture());
        SecurityContext newContext = captor.getValue();
        assertThat(newContext.getUserPrincipal()).isInstanceOf(JWTUserPrincipal.class);
        assertThat(newContext.isUserInRole("READ")).isFalse();
        assertThat(newContext.isUserInRole("CREATE")).isFalse();
    }

    private static SecurityContext fakeCurrentSecurityContext(boolean secure) {
        return new SecurityContext() {
            @Override
            public java.security.Principal getUserPrincipal() {
                return null;
            }

            @Override
            public boolean isUserInRole(String role) {
                return false;
            }

            @Override
            public boolean isSecure() {
                return secure;
            }

            @Override
            public String getAuthenticationScheme() {
                return null;
            }
        };
    }
}
