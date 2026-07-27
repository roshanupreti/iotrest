package com.project.iotrest.service.particle;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.iotrest.exceptions.ApplicationException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the real {@link ParticleClient} (not mocked) against a local {@link MockWebServer},
 * using the test-only fixtures at {@code riot/src/test/resources/conf/particle/}.
 */
class ParticleClientTest {

    private MockWebServer server;
    private ParticleClient particleClient;

    @BeforeEach
    void setUp() throws MalformedURLException, IOException {
        server = new MockWebServer();
        server.start();
        particleClient = new ParticleClient();
    }

    @AfterEach
    void tearDown() {
        try {
            server.shutdown();
        } catch (IOException alreadyShutDown) {
            // The connection-failure test shuts the server down itself to simulate an
            // unreachable host; a redundant shutdown here is expected in that case.
        }
    }

    @Test
    void executeRequest_getSuccess_returnsParsedJsonBody() throws ApplicationException, InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"connected\":true}"));

        JsonNode result = particleClient.executeRequest(
                server.url("/v1/devices/abc").toString(), "GET", "application/json", "",
                Map.of("Content-Type", "application/json"));

        assertThat(result.get("connected").asBoolean()).isTrue();
        RecordedRequest recorded = server.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("GET");
        assertThat(recorded.getHeader("Content-Type")).isEqualTo("application/json");
    }

    @Test
    void executeRequest_postSendsBodyAndContentType() throws ApplicationException, InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"return_value\":1}"));

        JsonNode result = particleClient.executeRequest(
                server.url("/v1/devices/abc/led").toString(), "POST",
                "application/x-www-form-urlencoded", "arg=red",
                Map.of("Content-Type", "application/x-www-form-urlencoded"));

        assertThat(result.get("return_value").asInt()).isEqualTo(1);
        RecordedRequest recorded = server.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getBody().readUtf8()).isEqualTo("arg=red");
        // OkHttp's RequestBody.create(String, MediaType) appends "; charset=utf-8" to the
        // media type it actually sends when none was specified -- expected OkHttp behaviour,
        // not something under this class's control.
        assertThat(recorded.getHeader("Content-Type")).startsWith("application/x-www-form-urlencoded");
    }

    @Test
    void executeRequest_nonSuccessResponse_throwsSanitizedApplicationException() {
        server.enqueue(new MockResponse().setResponseCode(404).setBody("not found"));
        String urlWithToken = server.url("/v1/devices/abc").newBuilder()
                .addQueryParameter("access_token", "super-secret-token-value")
                .build().toString();

        assertThatThrownBy(() -> particleClient.executeRequest(
                urlWithToken, "GET", "application/json", "", Collections.emptyMap()))
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> {
                    ApplicationException ex = (ApplicationException) e;
                    assertThat(ex.getStatusCode()).isEqualTo(500);
                    assertThat(ex.getResponseMessage()).contains("404");
                    // Regression guard for the token-leak fix: the sanitized error message must
                    // never echo the request URL (and therefore never the access_token it carried).
                    assertThat(ex.getResponseMessage()).doesNotContain("super-secret-token-value");
                    assertThat(ex.getResponseMessage()).doesNotContain("access_token");
                });
    }

    @Test
    void executeRequest_connectionFailure_wrapsAsApplicationException() throws IOException {
        String unreachableUrl = server.url("/v1/devices/abc").toString();
        server.shutdown();

        assertThatThrownBy(() -> particleClient.executeRequest(
                unreachableUrl, "GET", "application/json", "", Collections.emptyMap()))
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> assertThat(((ApplicationException) e).getStatusCode()).isEqualTo(500));
    }
}
