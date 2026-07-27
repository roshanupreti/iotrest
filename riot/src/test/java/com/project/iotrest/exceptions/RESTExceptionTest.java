package com.project.iotrest.exceptions;

import com.project.iotrest.exceptions.wrapper.ErrorWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * {@link RESTException} plays a dual role: it is both the exception actually thrown by
 * application code (constructed via {@code new RESTException(code, message)}, with no
 * {@code @Context} fields populated) and, separately, the JAX-RS-managed {@code @Provider}
 * singleton whose {@code @Context}-injected {@code uriInfo}/{@code request} fields are used
 * by {@link RESTException#toResponse}. This test mirrors that split by injecting the mocks
 * into a distinct "mapper" instance from the "thrown" instance, exactly as Jersey would.
 */
@ExtendWith(MockitoExtension.class)
class RESTExceptionTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private UriInfo uriInfo;

    private RESTException mapper;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        mapper = new RESTException();
        setPrivateField(mapper, "request", request);
        setPrivateField(mapper, "uriInfo", uriInfo);
    }

    private static void setPrivateField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = RESTException.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void getters_returnConstructorValues() {
        RESTException exception = new RESTException(404, "Not found");

        assertThat(exception.getStatusCode()).isEqualTo(404);
        assertThat(exception.getResponseMessage()).isEqualTo("Not found");
    }

    @Test
    void noArgConstructor_leavesFieldsNull() {
        RESTException exception = new RESTException();

        assertThat(exception.getStatusCode()).isNull();
        assertThat(exception.getResponseMessage()).isNull();
    }

    @Test
    void toResponse_buildsResponseMatchingStatusCodeAndErrorWrapper() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getServletPath()).thenReturn("/riot");
        when(request.getPathInfo()).thenReturn("/devices/42");
        RESTException thrown = new RESTException(404, "Device not found");

        Response response = mapper.toResponse(thrown);

        assertThat(response.getStatus()).isEqualTo(404);
        ErrorWrapper body = (ErrorWrapper) response.getEntity();
        assertThat(body.code()).isEqualTo(404);
        assertThat(body.status()).isEqualTo("Not Found");
        assertThat(body.reason()).isEqualTo("Device not found");
        assertThat(body.action()).isEqualTo("GET");
        assertThat(body.apiPath()).isEqualTo("/riot/devices/42");
    }

    @Test
    void toResponse_forbidden_reflectsStatus403() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/riot");
        when(request.getPathInfo()).thenReturn("/users");
        RESTException thrown = new RESTException(403, "You do not have enough privileges to perform this action.");

        Response response = mapper.toResponse(thrown);

        assertThat(response.getStatus()).isEqualTo(403);
        ErrorWrapper body = (ErrorWrapper) response.getEntity();
        assertThat(body.status()).isEqualTo("Forbidden");
    }
}
