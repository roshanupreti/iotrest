package com.project.iotrest.rest.devices;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.service.devices.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private DeviceService deviceService;

    private DeviceResource resource;

    @BeforeEach
    void setUp() {
        resource = new DeviceResource(deviceService);
    }

    @Test
    void getDevices_happyPath_returnsOkWithServiceResult() throws ApplicationException {
        JsonNode devices = MAPPER.createArrayNode().add("device-1");
        when(deviceService.getDevices()).thenReturn(devices);

        Response response = resource.getDevices();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(devices);
    }

    @Test
    void getDevices_serviceThrows_propagatesAsRestException() throws ApplicationException {
        when(deviceService.getDevices()).thenThrow(new ApplicationException(500, "boom"));

        assertThatThrownBy(() -> resource.getDevices())
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getStatusCode()).isEqualTo(500));
    }

    @Test
    void getDeviceById_happyPath_returnsOkWithServiceResult() throws ApplicationException {
        JsonNode device = MAPPER.createObjectNode().put("id", "device-1");
        when(deviceService.getDeviceInfo("device-1")).thenReturn(device);

        Response response = resource.getDeviceById("device-1");

        assertThat(response.getEntity()).isEqualTo(device);
    }

    @Test
    void getDeviceById_serviceThrows_propagatesAsRestException() throws ApplicationException {
        when(deviceService.getDeviceInfo("missing")).thenThrow(new ApplicationException(500, "not found upstream"));

        assertThatThrownBy(() -> resource.getDeviceById("missing"))
                .isInstanceOf(RESTException.class);
    }

    @Test
    void getVariableValue_happyPath_returnsOkWithServiceResult() throws ApplicationException {
        JsonNode variable = MAPPER.createObjectNode().put("result", 139);
        when(deviceService.getVariableValue("device-1", "distance")).thenReturn(variable);

        Response response = resource.getVariableValue("device-1", "distance");

        assertThat(response.getEntity()).isEqualTo(variable);
    }

    @Test
    void getVariableValue_serviceThrows_propagatesAsRestException() throws ApplicationException {
        when(deviceService.getVariableValue("device-1", "nonexistent"))
                .thenThrow(new ApplicationException(500, "Particle Cloud request failed with HTTP 404 "));

        assertThatThrownBy(() -> resource.getVariableValue("device-1", "nonexistent"))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getStatusCode()).isEqualTo(500));
    }

    @Test
    void callFunction_happyPath_returnsOkWithServiceResult() throws ApplicationException {
        JsonNode result = MAPPER.createObjectNode().put("return_value", 1);
        when(deviceService.callFunction("device-1", "led", "red")).thenReturn(result);

        Response response = resource.callFunction("device-1", "led", "red");

        assertThat(response.getEntity()).isEqualTo(result);
    }

    @Test
    void callFunction_serviceThrows_propagatesAsRestException() throws ApplicationException {
        when(deviceService.callFunction("device-1", "led", "red"))
                .thenThrow(new ApplicationException(500, "boom"));

        assertThatThrownBy(() -> resource.callFunction("device-1", "led", "red"))
                .isInstanceOf(RESTException.class);
    }
}
