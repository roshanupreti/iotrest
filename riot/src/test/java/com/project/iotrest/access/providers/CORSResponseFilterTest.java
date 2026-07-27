package com.project.iotrest.access.providers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CORSResponseFilterTest {

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private ContainerResponseContext responseContext;

    @Test
    void filter_addsExpectedCorsHeaders() {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);

        new CORSResponseFilter().filter(requestContext, responseContext);

        assertThat(headers.getFirst("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(headers.getFirst("Access-Control-Allow-Methods")).isEqualTo("GET, POST, DELETE, PUT");
        assertThat(headers.getFirst("Access-Control-Allow-Headers")).isEqualTo("*");
    }
}
