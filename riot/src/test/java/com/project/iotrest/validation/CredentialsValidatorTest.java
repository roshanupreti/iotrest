package com.project.iotrest.validation;

import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.LoginCredentials;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CredentialsValidatorTest {

    @Test
    void validateLoginCredentials_null_throwsBadRequest() {
        assertThatThrownBy(() -> CredentialsValidator.validateLoginCredentials(null))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getStatusCode()).isEqualTo(400));
    }

    @Test
    void validateLoginCredentials_missingIdentifier_throws() {
        assertThatThrownBy(() -> CredentialsValidator.validateLoginCredentials(new LoginCredentials("", "secret")))
                .isInstanceOf(RESTException.class);
    }

    @Test
    void validateLoginCredentials_missingPassword_throws() {
        assertThatThrownBy(() -> CredentialsValidator.validateLoginCredentials(new LoginCredentials("john", "")))
                .isInstanceOf(RESTException.class);
    }

    @Test
    void validateLoginCredentials_bothMissing_throws() {
        assertThatThrownBy(() -> CredentialsValidator.validateLoginCredentials(new LoginCredentials(null, null)))
                .isInstanceOf(RESTException.class);
    }

    @Test
    void validateLoginCredentials_valid_doesNotThrow() {
        CredentialsValidator.validateLoginCredentials(new LoginCredentials("john", "secret"));
    }

    @Test
    void hashPassword_producesVerifiableBCryptHash() {
        String plaintext = "secret";
        String hashed = CredentialsValidator.hashPassword(plaintext);

        assertThat(hashed).isNotEqualTo(plaintext);
        assertThat(BCrypt.checkpw(plaintext, hashed)).isTrue();
    }

    @Test
    void hashPassword_saltsEachCallDifferently() {
        String first = CredentialsValidator.hashPassword("secret");
        String second = CredentialsValidator.hashPassword("secret");

        assertThat(first).isNotEqualTo(second);
    }
}
