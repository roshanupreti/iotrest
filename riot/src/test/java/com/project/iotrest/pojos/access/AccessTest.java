package com.project.iotrest.pojos.access;

import com.project.iotrest.exceptions.RESTException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccessTest {

    @ParameterizedTest
    @CsvSource({
            "create, CREATE",
            "CREATE, CREATE",
            "read, READ",
            "update, UPDATE",
            "delete, DELETE",
            "DeLeTe, DELETE"
    })
    void fromString_resolvesCaseInsensitively(String input, Access expected) {
        assertThat(Access.fromString(input)).isEqualTo(expected);
    }

    @Test
    void fromString_unknownValue_throwsBadRequest() {
        assertThatThrownBy(() -> Access.fromString("nonexistent"))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getStatusCode()).isEqualTo(400));
    }

    @Test
    void getLevel_returnsLowercaseLevelString() {
        assertThat(Access.CREATE.getLevel()).isEqualTo("create");
        assertThat(Access.READ.getLevel()).isEqualTo("read");
        assertThat(Access.UPDATE.getLevel()).isEqualTo("update");
        assertThat(Access.DELETE.getLevel()).isEqualTo("delete");
    }
}
