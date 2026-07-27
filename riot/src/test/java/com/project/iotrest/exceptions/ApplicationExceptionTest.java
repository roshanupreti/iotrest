package com.project.iotrest.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationExceptionTest {

    @Test
    void getters_returnConstructorValues() {
        ApplicationException exception = new ApplicationException(404, "Not found");

        assertThat(exception.getStatusCode()).isEqualTo(404);
        assertThat(exception.getResponseMessage()).isEqualTo("Not found");
        assertThat(exception.getMessage()).isEqualTo("Not found");
    }
}
