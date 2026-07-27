package com.project.iotrest.security;

import com.project.iotrest.pojos.access.Access;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JWTUserPrincipalTest {

    @Test
    void getName_returnsUsername() {
        JWTUserPrincipal principal = new JWTUserPrincipal(1, "john", "john@example.org", Set.of(Access.READ));

        assertThat(principal.getName()).isEqualTo("john");
    }

    @Test
    void hasAccess_returnsTrueWhenLevelPresent() {
        JWTUserPrincipal principal = new JWTUserPrincipal(1, "john", "j@x.com", EnumSet.of(Access.READ, Access.CREATE));

        assertThat(principal.hasAccess("READ")).isTrue();
        assertThat(principal.hasAccess("CREATE")).isTrue();
    }

    @Test
    void hasAccess_returnsFalseWhenLevelAbsent() {
        JWTUserPrincipal principal = new JWTUserPrincipal(1, "john", "j@x.com", EnumSet.of(Access.READ));

        assertThat(principal.hasAccess("DELETE")).isFalse();
    }

    @Test
    void hasAccess_returnsFalseForUnknownAccessLevelString() {
        JWTUserPrincipal principal = new JWTUserPrincipal(1, "john", "j@x.com", EnumSet.of(Access.READ));

        assertThat(principal.hasAccess("NOT_A_REAL_LEVEL")).isFalse();
    }

    @Test
    void hasAccess_returnsFalseWhenAccessLevelsIsNull() {
        JWTUserPrincipal principal = new JWTUserPrincipal(1, "john", "j@x.com", null);

        assertThat(principal.hasAccess("READ")).isFalse();
    }

    @Test
    void equals_sameIdAndUsername_areEqual() {
        JWTUserPrincipal a = new JWTUserPrincipal(1, "john", "a@x.com", Set.of(Access.READ));
        JWTUserPrincipal b = new JWTUserPrincipal(1, "john", "different-email@x.com", Set.of(Access.DELETE));

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void equals_differentUsername_areNotEqual() {
        JWTUserPrincipal a = new JWTUserPrincipal(1, "john", "a@x.com", Set.of(Access.READ));
        JWTUserPrincipal b = new JWTUserPrincipal(1, "jane", "a@x.com", Set.of(Access.READ));

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void equals_null_returnsFalse() {
        JWTUserPrincipal a = new JWTUserPrincipal(1, "john", "a@x.com", Set.of(Access.READ));

        assertThat(a).isNotEqualTo(null);
    }

    @Test
    void equals_differentClass_returnsFalse() {
        JWTUserPrincipal a = new JWTUserPrincipal(1, "john", "a@x.com", Set.of(Access.READ));

        assertThat(a).isNotEqualTo("not a principal");
    }

    @Test
    void toString_doesNotThrowAndContainsUsername() {
        JWTUserPrincipal principal = new JWTUserPrincipal(1, "john", "a@x.com", Set.of(Access.READ));

        assertThat(principal.toString()).contains("john");
    }
}
