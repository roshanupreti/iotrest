package com.project.iotrest.rest.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.LoginCredentials;
import com.project.iotrest.pojos.user.User;
import com.project.iotrest.service.token.TokenService;
import com.project.iotrest.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    private AuthenticationResource resource;

    @BeforeEach
    void setUp() {
        resource = new AuthenticationResource(userService, tokenService);
    }

    @Test
    void authenticate_validCredentials_returnsGeneratedToken() throws ApplicationException {
        User user = new User(1, "First", "Last", "john", "john@example.org", "hashed", true, null);
        JsonNode tokenNode = MAPPER.createObjectNode().put("access_token", "signed.jwt.token");
        when(userService.attemptUserLogin(new LoginCredentials("john", "secret"))).thenReturn(user);
        when(tokenService.generateToken(user)).thenReturn(tokenNode);

        Response response = resource.authenticate(new LoginCredentials("john", "secret"));

        assertThat(response.getEntity()).isEqualTo(tokenNode);
    }

    @Test
    void authenticate_missingCredentials_rejectedBeforeReachingUserService() {
        assertThatThrownBy(() -> resource.authenticate(new LoginCredentials("", "")))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getStatusCode()).isEqualTo(400));

        verifyNoInteractions(userService, tokenService);
    }

    @Test
    void authenticate_incorrectPassword_propagatesAsRestException() throws ApplicationException {
        when(userService.attemptUserLogin(new LoginCredentials("john", "wrong")))
                .thenThrow(new ApplicationException(400, "Incorrect Password."));

        assertThatThrownBy(() -> resource.authenticate(new LoginCredentials("john", "wrong")))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> {
                    assertThat(((RESTException) e).getStatusCode()).isEqualTo(400);
                    assertThat(((RESTException) e).getResponseMessage()).isEqualTo("Incorrect Password.");
                });
    }
}
