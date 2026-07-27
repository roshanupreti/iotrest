package com.project.iotrest.service.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.iotrest.pojos.access.Access;
import com.project.iotrest.pojos.access.AuthenticationTokenParams;
import com.project.iotrest.pojos.user.User;
import com.project.iotrest.pojos.user.UserAccessRights;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JWTProvider jwtProvider;

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(jwtProvider);
    }

    private static User userWith(UserAccessRights rights) {
        return new User(1, "First", "Last", "john", "john@example.org", "hashed", true, rights);
    }

    @Test
    void generateToken_wrapsIssuedJwtAsAccessTokenField() {
        when(jwtProvider.issueJWT(any())).thenReturn("signed.jwt.token");

        JsonNode result = tokenService.generateToken(userWith(new UserAccessRights(1, true, true, true, true)));

        assertThat(result.get("access_token").asText()).isEqualTo("signed.jwt.token");
    }

    @Test
    void generateToken_passesUserIdentityToJwtProvider() {
        when(jwtProvider.issueJWT(any())).thenReturn("token");

        tokenService.generateToken(userWith(new UserAccessRights(1, true, false, false, false)));

        ArgumentCaptor<AuthenticationTokenParams> captor = ArgumentCaptor.forClass(AuthenticationTokenParams.class);
        verify(jwtProvider).issueJWT(captor.capture());

        AuthenticationTokenParams params = captor.getValue();
        assertThat(params.id()).isEqualTo("1");
        assertThat(params.username()).isEqualTo("john");
        assertThat(params.email()).isEqualTo("john@example.org");
        assertThat(params.accessRights()).containsExactly(Access.CREATE);
        assertThat(params.issuedDate()).isBefore(params.expirationDate());
    }

    @Test
    void getAccessSet_allTrue_returnsAllFourLevels() {
        Set<Access> result = tokenService.getAccessSet(new UserAccessRights(1, true, true, true, true));

        assertThat(result).containsExactlyInAnyOrder(Access.CREATE, Access.READ, Access.UPDATE, Access.DELETE);
    }

    @Test
    void getAccessSet_allFalse_returnsEmptySet() {
        Set<Access> result = tokenService.getAccessSet(new UserAccessRights(1, false, false, false, false));

        assertThat(result).isEmpty();
    }

    @Test
    void getAccessSet_allNull_returnsEmptySet() {
        Set<Access> result = tokenService.getAccessSet(new UserAccessRights(1, null, null, null, null));

        assertThat(result).isEmpty();
    }

    @Test
    void getAccessSet_mixed_returnsOnlyTrueLevels() {
        Set<Access> result = tokenService.getAccessSet(new UserAccessRights(1, true, false, true, null));

        assertThat(result).containsExactlyInAnyOrder(Access.CREATE, Access.UPDATE);
    }
}
