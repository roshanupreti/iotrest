package com.project.iotrest.pojos.access;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationTokenParamsTest {

    @Test
    void builder_populatesAllFields() {
        ZonedDateTime issued = ZonedDateTime.now();
        ZonedDateTime expiration = issued.plusMinutes(15);

        AuthenticationTokenParams params = new AuthenticationTokenParams.Builder()
                .withId("1")
                .withUsername("john")
                .withEmail("john@example.org")
                .withAccessRights(Set.of(Access.READ))
                .withIssuedDate(issued)
                .withExpirationDate(expiration)
                .build();

        assertThat(params.id()).isEqualTo("1");
        assertThat(params.username()).isEqualTo("john");
        assertThat(params.email()).isEqualTo("john@example.org");
        assertThat(params.accessRights()).containsExactly(Access.READ);
        assertThat(params.issuedDate()).isEqualTo(issued);
        assertThat(params.expirationDate()).isEqualTo(expiration);
    }

    @Test
    void builder_nullAccessRights_defaultsToEmptySet() {
        AuthenticationTokenParams params = new AuthenticationTokenParams.Builder()
                .withId("1")
                .withUsername("john")
                .withEmail("john@example.org")
                .withAccessRights(null)
                .withIssuedDate(ZonedDateTime.now())
                .withExpirationDate(ZonedDateTime.now().plusMinutes(15))
                .build();

        assertThat(params.accessRights()).isEmpty();
    }

    @Test
    void builder_accessRightsSet_isUnmodifiable() {
        AuthenticationTokenParams params = new AuthenticationTokenParams.Builder()
                .withAccessRights(Set.of(Access.READ))
                .build();

        assertThat(params.accessRights()).isUnmodifiable();
    }
}
