package com.project.iotrest.service.token;

import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.Access;
import com.project.iotrest.pojos.access.AuthenticationTokenParams;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the real {@link JWTProvider} (not mocked) against the test-only signing secret at
 * {@code riot/src/test/resources/conf/jwt/jwt.properties}, resolved relative to Surefire's
 * configured working directory (see the {@code maven-surefire-plugin} config in {@code riot/pom.xml}).
 */
class JWTProviderTest {

    private JWTProvider jwtProvider;

    @BeforeEach
    void setUp() throws MalformedURLException {
        jwtProvider = new JWTProvider();
    }

    private AuthenticationTokenParams paramsWith(ZonedDateTime issued, ZonedDateTime expiration) {
        return new AuthenticationTokenParams.Builder()
                .withId("1")
                .withUsername("john")
                .withEmail("john@example.org")
                .withAccessRights(Set.of(Access.READ, Access.CREATE))
                .withIssuedDate(issued)
                .withExpirationDate(expiration)
                .build();
    }

    @Test
    void issueJWT_producesThreePartToken() {
        String token = jwtProvider.issueJWT(paramsWith(ZonedDateTime.now(), ZonedDateTime.now().plusMinutes(15)));

        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void issueThenParse_roundTripsExpectedClaims() {
        String token = jwtProvider.issueJWT(paramsWith(ZonedDateTime.now(), ZonedDateTime.now().plusMinutes(15)));

        Claims claims = jwtProvider.parseAndValidateToken(token);

        assertThat(claims.getSubject()).isEqualTo("john");
        assertThat(claims.get("id", Integer.class)).isEqualTo(1);
        assertThat(claims.get("email", String.class)).isEqualTo("john@example.org");
        @SuppressWarnings("unchecked")
        List<String> accessLevels = claims.get("access-levels", List.class);
        assertThat(accessLevels).containsExactlyInAnyOrder("READ", "CREATE");
    }

    @Test
    void parseAndValidateToken_nullToken_throwsMissingJwt() {
        assertThatThrownBy(() -> jwtProvider.parseAndValidateToken(null))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> {
                    assertThat(((RESTException) e).getStatusCode()).isEqualTo(401);
                    assertThat(((RESTException) e).getResponseMessage()).isEqualTo("Missing JWT.");
                });
    }

    @Test
    void parseAndValidateToken_emptyToken_throwsMissingJwt() {
        assertThatThrownBy(() -> jwtProvider.parseAndValidateToken(""))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getResponseMessage()).isEqualTo("Missing JWT."));
    }

    @Test
    void parseAndValidateToken_malformedToken_throwsInvalidToken() {
        assertThatThrownBy(() -> jwtProvider.parseAndValidateToken("not-a-real-jwt"))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> {
                    assertThat(((RESTException) e).getStatusCode()).isEqualTo(401);
                    assertThat(((RESTException) e).getResponseMessage()).isEqualTo("Invalid token.");
                });
    }

    @Test
    void parseAndValidateToken_wrongSigningSecret_throwsInvalidToken() {
        String tokenSignedWithDifferentSecret = Jwts.builder()
                .setSubject("john")
                .signWith(SignatureAlgorithm.HS256, "a-completely-different-secret-value")
                .compact();

        assertThatThrownBy(() -> jwtProvider.parseAndValidateToken(tokenSignedWithDifferentSecret))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getResponseMessage()).isEqualTo("Invalid token."));
    }

    @Test
    void parseAndValidateToken_expiredToken_throwsExpiredToken() {
        ZonedDateTime issued = ZonedDateTime.now().minusHours(2);
        ZonedDateTime expired = issued.plusMinutes(15);
        String token = jwtProvider.issueJWT(paramsWith(issued, expired));

        assertThatThrownBy(() -> jwtProvider.parseAndValidateToken(token))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> {
                    assertThat(((RESTException) e).getStatusCode()).isEqualTo(401);
                    assertThat(((RESTException) e).getResponseMessage()).isEqualTo("Expired token.");
                });
    }
}
