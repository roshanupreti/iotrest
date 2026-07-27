package com.project.iotrest.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorStatusCodesTest {

    @Test
    void codes_matchExpectedHttpStatusValues() {
        assertThat(ErrorStatusCodes.NOT_FOUND.getCode()).isEqualTo(404);
        assertThat(ErrorStatusCodes.BAD_REQUEST.getCode()).isEqualTo(400);
        assertThat(ErrorStatusCodes.UNAUTHORIZED.getCode()).isEqualTo(401);
        assertThat(ErrorStatusCodes.FORBIDDEN.getCode()).isEqualTo(403);
        assertThat(ErrorStatusCodes.SERVER_ERROR.getCode()).isEqualTo(500);
    }
}
