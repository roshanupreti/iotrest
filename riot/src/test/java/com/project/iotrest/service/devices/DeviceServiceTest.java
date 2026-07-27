package com.project.iotrest.service.devices;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.service.particle.Keys;
import com.project.iotrest.service.particle.ParticleClient;
import com.project.iotrest.service.token.ParticleLoginService;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String API_BASE = "https://api.particle.io";
    private static final String DEVICES_BASE = "/v1/devices";

    @Mock
    private ParticleLoginService particleLoginService;

    @Mock
    private ParticleClient particleClient;

    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        deviceService = new DeviceService(particleLoginService, particleClient);
        when(particleClient.getApiBaseMap()).thenReturn(Map.of(
                Keys.PARTICLE_API_BASE.getKey(), API_BASE,
                Keys.PARTICLE_DEVICES_BASE.getKey(), DEVICES_BASE));
    }

    private static JsonNode fakeResponse() {
        return MAPPER.createObjectNode().put("ok", true);
    }

    @Test
    void getDevices_callsListUrlWithAccessTokenAndReturnsResponse() throws ApplicationException {
        when(particleLoginService.getAccessToken()).thenReturn("tok-123");
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(fakeResponse());

        JsonNode result = deviceService.getDevices();

        assertThat(result.get("ok").asBoolean()).isTrue();
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(particleClient).executeRequest(urlCaptor.capture(), org.mockito.ArgumentMatchers.eq("GET"),
                anyString(), anyString(), anyMap());
        HttpUrl url = HttpUrl.parse(urlCaptor.getValue());
        assertThat(url).isNotNull();
        assertThat(url.toString()).startsWith(API_BASE + DEVICES_BASE);
        assertThat(url.queryParameter("access_token")).isEqualTo("tok-123");
    }

    @Test
    void getDeviceInfo_includesDeviceIdInPath() throws ApplicationException {
        when(particleLoginService.getAccessToken()).thenReturn("tok-123");
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(fakeResponse());

        deviceService.getDeviceInfo("device-42");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(particleClient).executeRequest(urlCaptor.capture(), anyString(), anyString(), anyString(), anyMap());
        HttpUrl url = HttpUrl.parse(urlCaptor.getValue());
        assertThat(url).isNotNull();
        assertThat(url.encodedPath()).endsWith("/device-42");
    }

    @Test
    void getVariableValue_includesDeviceIdAndVariableNameInPath() throws ApplicationException {
        when(particleLoginService.getAccessToken()).thenReturn("tok-123");
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(fakeResponse());

        deviceService.getVariableValue("device-42", "distance");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(particleClient).executeRequest(urlCaptor.capture(), org.mockito.ArgumentMatchers.eq("GET"),
                anyString(), anyString(), anyMap());
        HttpUrl url = HttpUrl.parse(urlCaptor.getValue());
        assertThat(url).isNotNull();
        assertThat(url.encodedPath()).endsWith("/device-42/distance");
    }

    @Test
    void callFunction_sendsPostWithFormUrlEncodedArg() throws ApplicationException {
        when(particleLoginService.getAccessToken()).thenReturn("tok-123");
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(fakeResponse());

        deviceService.callFunction("device-42", "led", "red");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> methodCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> mediaTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(particleClient).executeRequest(urlCaptor.capture(), methodCaptor.capture(),
                mediaTypeCaptor.capture(), bodyCaptor.capture(), anyMap());

        assertThat(methodCaptor.getValue()).isEqualTo("POST");
        assertThat(mediaTypeCaptor.getValue()).isEqualTo("application/x-www-form-urlencoded");
        assertThat(bodyCaptor.getValue()).isEqualTo("arg=red");
        HttpUrl url = HttpUrl.parse(urlCaptor.getValue());
        assertThat(url).isNotNull();
        assertThat(url.encodedPath()).endsWith("/device-42/led");
    }

    @Test
    void callFunction_nullArg_sendsEmptyArgValue() throws ApplicationException {
        when(particleLoginService.getAccessToken()).thenReturn("tok-123");
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(fakeResponse());

        deviceService.callFunction("device-42", "led", null);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(particleClient).executeRequest(anyString(), anyString(), anyString(), bodyCaptor.capture(), anyMap());
        assertThat(bodyCaptor.getValue()).isEqualTo("arg=");
    }

    @Test
    void callFunction_argWithSpecialCharacters_isUrlEncoded() throws ApplicationException {
        when(particleLoginService.getAccessToken()).thenReturn("tok-123");
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(fakeResponse());

        deviceService.callFunction("device-42", "led", "a b&c=d");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(particleClient).executeRequest(anyString(), anyString(), anyString(), bodyCaptor.capture(), anyMap());
        assertThat(bodyCaptor.getValue()).isEqualTo("arg=a+b%26c%3Dd");
    }

    @Test
    void getDevices_propagatesApplicationExceptionFromParticleClient() throws ApplicationException {
        when(particleLoginService.getAccessToken()).thenReturn("tok-123");
        when(particleClient.executeRequest(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenThrow(new ApplicationException(500, "boom"));

        assertThatThrownBy(() -> deviceService.getDevices())
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> assertThat(((ApplicationException) e).getResponseMessage()).isEqualTo("boom"));
    }
}
