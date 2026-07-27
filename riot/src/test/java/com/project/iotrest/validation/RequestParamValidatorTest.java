package com.project.iotrest.validation;

import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.user.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestParamValidatorTest {

    private static User userWith(String userName, String email) {
        return new User(1, "First", "Last", userName, email, "secret", true, null);
    }

    @Test
    void validateId_null_throwsBadRequest() {
        assertThatThrownBy(() -> RequestParamValidator.validateId(null))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getStatusCode()).isEqualTo(400));
    }

    @Test
    void validateId_zero_throws() {
        assertThatThrownBy(() -> RequestParamValidator.validateId(0))
                .isInstanceOf(RESTException.class);
    }

    @Test
    void validateId_positive_doesNotThrow() {
        assertThatCode(() -> RequestParamValidator.validateId(1)).doesNotThrowAnyException();
    }

    @Test
    void validateId_negative_currentlyDoesNotThrow() {
        // Documents existing behaviour: only null/zero are rejected, negative ids pass through
        // unvalidated. Not necessarily desirable, but this is what the code does today.
        assertThatCode(() -> RequestParamValidator.validateId(-1)).doesNotThrowAnyException();
    }

    @Test
    void validateString_empty_throws() {
        assertThatThrownBy(() -> RequestParamValidator.validateString(""))
                .isInstanceOf(RESTException.class);
    }

    @Test
    void validateString_null_throws() {
        assertThatThrownBy(() -> RequestParamValidator.validateString(null))
                .isInstanceOf(RESTException.class);
    }

    @Test
    void validateString_nonEmpty_doesNotThrow() {
        assertThatCode(() -> RequestParamValidator.validateString("abc")).doesNotThrowAnyException();
    }

    @Test
    void validatePost_missingUsername_throws() {
        assertThatThrownBy(() -> RequestParamValidator.validatePost(userWith("", "a@b.com")))
                .isInstanceOf(RESTException.class);
    }

    @Test
    void validatePost_missingEmail_throws() {
        assertThatThrownBy(() -> RequestParamValidator.validatePost(userWith("john", "")))
                .isInstanceOf(RESTException.class);
    }

    @Test
    void validatePost_missingBoth_throws() {
        assertThatThrownBy(() -> RequestParamValidator.validatePost(userWith(null, null)))
                .isInstanceOf(RESTException.class);
    }

    @Test
    void validatePost_valid_doesNotThrow() {
        assertThatCode(() -> RequestParamValidator.validatePost(userWith("john", "john@example.org")))
                .doesNotThrowAnyException();
    }
}
