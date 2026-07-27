package com.project.iotrest.service.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.service.particle.Keys;
import com.project.iotrest.service.particle.ParticleClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticleLoginServiceImplTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private ParticleClient particleClient;

    private ParticleLoginServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ParticleLoginServiceImpl(particleClient);
    }

    private static JsonNode tokenResponse(String accessToken, Long expiresIn) {
        var node = MAPPER.createObjectNode();
        node.put("access_token", accessToken);
        if (expiresIn != null) {
            node.put("expires_in", expiresIn);
        }
        return node;
    }

    @Test
    void getAccessToken_firstCall_refreshesAndReturnsToken() throws ApplicationException {
        when(particleClient.getApiBaseMap()).thenReturn(Map.of(Keys.PARTICLE_LOGIN_BASE.getKey(), "https://api.particle.io/oauth/token"));
        when(particleClient.getCredsMap()).thenReturn(Map.of("client_id", "abc", "client_secret", "xyz"));
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(tokenResponse("real-access-token", 3600L));

        String token = service.getAccessToken();

        assertThat(token).isEqualTo("real-access-token");
        verify(particleClient, times(1)).executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void getAccessToken_secondCallBeforeExpiry_usesCachedTokenWithoutRefreshing() throws ApplicationException {
        when(particleClient.getApiBaseMap()).thenReturn(Map.of(Keys.PARTICLE_LOGIN_BASE.getKey(), "https://api.particle.io/oauth/token"));
        when(particleClient.getCredsMap()).thenReturn(Map.of("client_id", "abc"));
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(tokenResponse("cached-token", 3600L));

        String first = service.getAccessToken();
        String second = service.getAccessToken();

        assertThat(first).isEqualTo(second);
        verify(particleClient, times(1)).executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void getAccessToken_tokenExpiresImmediately_refreshesAgainOnNextCall() throws ApplicationException, InterruptedException {
        when(particleClient.getApiBaseMap()).thenReturn(Map.of(Keys.PARTICLE_LOGIN_BASE.getKey(), "https://api.particle.io/oauth/token"));
        when(particleClient.getCredsMap()).thenReturn(Map.of("client_id", "abc"));
        // expires_in <= the 30s safety buffer means the cached expiry is already in the past.
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(tokenResponse("short-lived-token", 1L));

        service.getAccessToken();
        Thread.sleep(5);
        service.getAccessToken();

        verify(particleClient, times(2)).executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void getAccessToken_missingExpiresInField_fallsBackToDefaultTtl() throws ApplicationException {
        when(particleClient.getApiBaseMap()).thenReturn(Map.of(Keys.PARTICLE_LOGIN_BASE.getKey(), "https://api.particle.io/oauth/token"));
        when(particleClient.getCredsMap()).thenReturn(Map.of("client_id", "abc"));
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(tokenResponse("token-no-ttl", null));

        service.getAccessToken();
        service.getAccessToken();

        // Default TTL (1 hour) comfortably outlives the test, so the second call must be cached.
        verify(particleClient, times(1)).executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void refreshAccessToken_propagatesApplicationExceptionFromParticleClient() throws ApplicationException {
        when(particleClient.getApiBaseMap()).thenReturn(Map.of(Keys.PARTICLE_LOGIN_BASE.getKey(), "https://api.particle.io/oauth/token"));
        when(particleClient.getCredsMap()).thenReturn(Map.of("client_id", "abc"));
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenThrow(new ApplicationException(500, "Particle Cloud request failed with HTTP 401 Unauthorized"));

        assertThatThrownBy(() -> service.refreshAccessToken())
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> assertThat(((ApplicationException) e).getStatusCode()).isEqualTo(500));
    }

    @Test
    void refreshAccessToken_bodyIsFormUrlEncodedFromCredsMap() throws ApplicationException {
        when(particleClient.getApiBaseMap()).thenReturn(Map.of(Keys.PARTICLE_LOGIN_BASE.getKey(), "https://api.particle.io/oauth/token"));
        when(particleClient.getCredsMap()).thenReturn(Map.of("client_id", "abc", "grant_type", "password"));
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(tokenResponse("token", 3600L));

        service.refreshAccessToken();

        var bodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(particleClient).executeRequest(anyString(), anyString(), anyString(), bodyCaptor.capture(), anyMap());
        assertThat(bodyCaptor.getValue()).contains("client_id=abc").contains("grant_type=password").contains("&");
    }
}
