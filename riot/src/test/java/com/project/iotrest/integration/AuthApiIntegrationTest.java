package com.project.iotrest.integration;

import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.LoginCredentials;
import com.project.iotrest.pojos.user.User;
import com.project.iotrest.pojos.user.UserAccessRights;
import com.project.iotrest.rest.auth.AuthenticationResource;
import com.project.iotrest.service.token.JWTProvider;
import com.project.iotrest.service.token.TokenService;
import com.project.iotrest.service.user.UserService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Real-HTTP integration test for the login endpoint, against a genuine embedded Jetty + Jersey
 * Servlet deployment (see {@link JerseyIntegrationTestSupport}). AuthenticationResource carries
 * no {@code @RequiredAccessType} annotation, so the (also registered) AuthenticationFilter's
 * name-binding correctly does not apply to it -- this test exercises that exact real behaviour,
 * not an assumption about it.
 */
class AuthApiIntegrationTest extends JerseyIntegrationTestSupport {

    private final UserService userService = Mockito.mock(UserService.class);
    private final TokenService tokenService = new TokenService(newJwtProvider());

    private static JWTProvider newJwtProvider() {
        try {
            return new JWTProvider();
        } catch (MalformedURLException e) {
            throw new AssertionError("Could not construct JWTProvider for integration test", e);
        }
    }

    @Override
    protected ResourceConfig resourceConfig() {
        return new ResourceConfig()
                .register(AuthenticationResource.class)
                .register(RESTException.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(userService).to(UserService.class);
                        bind(tokenService).to(TokenService.class);
                    }
                });
    }

    @BeforeEach
    void resetMock() {
        Mockito.reset(userService);
    }

    @Test
    void login_validCredentials_returns200WithSignedJwt() throws ApplicationException {
        User user = new User(1, "First", "Last", "john", "john@example.org", "hashed", true,
                new UserAccessRights(1, true, true, true, true));
        when(userService.attemptUserLogin(new LoginCredentials("john", "secret"))).thenReturn(user);

        Response response = target("auth/login").request()
                .post(Entity.entity(new LoginCredentials("john", "secret"), MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(200);
        String body = response.readEntity(String.class);
        assertThat(body).contains("access_token");
        String token = body.replaceAll(".*\"access_token\":\"([^\"]+)\".*", "$1");
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void login_missingCredentials_returns400() {
        Response response = target("auth/login").request()
                .post(Entity.entity(new LoginCredentials("", ""), MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void login_incorrectPassword_returns400WithMatchingMessage() throws ApplicationException {
        when(userService.attemptUserLogin(new LoginCredentials("john", "wrong")))
                .thenThrow(new ApplicationException(400, "Incorrect Password."));

        Response response = target("auth/login").request()
                .post(Entity.entity(new LoginCredentials("john", "wrong"), MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class)).contains("Incorrect Password.");
    }
}
