package com.project.iotrest.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.iotrest.access.providers.AuthenticationFilter;
import com.project.iotrest.access.providers.AuthorizationFilter;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.Access;
import com.project.iotrest.pojos.access.AuthenticationTokenParams;
import com.project.iotrest.rest.devices.DeviceResource;
import com.project.iotrest.service.devices.DeviceService;
import com.project.iotrest.service.token.JWTProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Real-HTTP integration tests against a genuine embedded Jetty + Jersey Servlet deployment (see
 * {@link JerseyIntegrationTestSupport}): requests go through the actual JAX-RS name-binding
 * mechanism ({@link AuthenticationFilter}/{@link AuthorizationFilter} wired exactly as
 * {@code JerseyAppConfig}'s package scan does in production), HK2-resolved constructor
 * injection, and the real {@link RESTException} exception mapper -- unlike the unit tests
 * elsewhere in this module, which call resource/filter methods directly with mocked collaborators.
 */
class DeviceApiIntegrationTest extends JerseyIntegrationTestSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JWTProvider jwtProvider = newJwtProvider();
    private final DeviceService deviceService = Mockito.mock(DeviceService.class);

    private static JWTProvider newJwtProvider() {
        try {
            return new JWTProvider();
        } catch (java.net.MalformedURLException e) {
            throw new AssertionError("Could not construct JWTProvider for integration test", e);
        }
    }

    @Override
    protected ResourceConfig resourceConfig() {
        return new ResourceConfig()
                .register(DeviceResource.class)
                .register(AuthenticationFilter.class)
                .register(AuthorizationFilter.class)
                .register(RESTException.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(jwtProvider).to(JWTProvider.class);
                        bind(deviceService).to(DeviceService.class);
                    }
                });
    }

    @BeforeEach
    void resetMock() {
        Mockito.reset(deviceService);
    }

    private String tokenWith(Access... accessRights) {
        ZonedDateTime now = ZonedDateTime.now();
        AuthenticationTokenParams params = new AuthenticationTokenParams.Builder()
                .withId("1")
                .withUsername("john")
                .withEmail("john@example.org")
                .withAccessRights(Set.of(accessRights))
                .withIssuedDate(now)
                .withExpirationDate(now.plusMinutes(15))
                .build();
        return jwtProvider.issueJWT(params);
    }

    @Test
    void getDevices_validReadToken_returns200WithServiceResult() throws ApplicationException {
        JsonNode devices = MAPPER.createArrayNode().add("device-1");
        when(deviceService.getDevices()).thenReturn(devices);

        Response response = target("devices").request()
                .header("Authorization", "Bearer " + tokenWith(Access.READ))
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("device-1");
    }

    @Test
    void getDevices_missingAuthorizationHeader_returns401WithErrorBody() {
        Response response = target("devices").request().get();

        assertThat(response.getStatus()).isEqualTo(401);
        String body = response.readEntity(String.class);
        assertThat(body).contains("Missing Authorization Header.");
    }

    @Test
    void getDevices_tokenWithoutReadAccess_returns403() {
        Response response = target("devices").request()
                .header("Authorization", "Bearer " + tokenWith(Access.CREATE))
                .get();

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.readEntity(String.class))
                .contains("You do not have enough privileges to perform this action.");
    }

    @Test
    void getDevices_expiredToken_returns401() {
        ZonedDateTime issued = ZonedDateTime.now().minusHours(2);
        AuthenticationTokenParams params = new AuthenticationTokenParams.Builder()
                .withId("1").withUsername("john").withEmail("john@example.org")
                .withAccessRights(Set.of(Access.READ))
                .withIssuedDate(issued).withExpirationDate(issued.plusMinutes(15))
                .build();
        String expiredToken = jwtProvider.issueJWT(params);

        Response response = target("devices").request()
                .header("Authorization", "Bearer " + expiredToken)
                .get();

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.readEntity(String.class)).contains("Expired token.");
    }

    @Test
    void callFunction_updateToken_returns200() throws ApplicationException {
        JsonNode result = MAPPER.createObjectNode().put("return_value", 1);
        when(deviceService.callFunction("device-1", "led", "red")).thenReturn(result);

        Response response = target("devices/device-1/functions/led").request()
                .header("Authorization", "Bearer " + tokenWith(Access.UPDATE))
                .post(Entity.entity("red", MediaType.TEXT_PLAIN));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("\"return_value\":1");
    }

    @Test
    void callFunction_readOnlyToken_returns403() {
        Response response = target("devices/device-1/functions/led").request()
                .header("Authorization", "Bearer " + tokenWith(Access.READ))
                .post(Entity.entity("red", MediaType.TEXT_PLAIN));

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void getDeviceInfo_serviceThrowsApplicationException_returnsMatchingStatusAndBody() throws ApplicationException {
        when(deviceService.getDeviceInfo("missing-device"))
                .thenThrow(new ApplicationException(500, "Particle Cloud request failed with HTTP 404 "));

        Response response = target("devices/missing-device").request()
                .header("Authorization", "Bearer " + tokenWith(Access.READ))
                .get();

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.readEntity(String.class)).contains("Particle Cloud request failed with HTTP 404");
    }
}
