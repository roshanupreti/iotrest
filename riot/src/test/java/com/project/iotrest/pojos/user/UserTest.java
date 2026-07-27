package com.project.iotrest.pojos.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void empty_producesAllNullFields() {
        User user = User.empty();

        assertThat(user.id()).isNull();
        assertThat(user.userName()).isNull();
        assertThat(user.userAccessRights()).isNull();
    }

    @Test
    void withPassword_returnsCopyWithOnlyPasswordChanged() {
        User original = new User(1, "First", "Last", "john", "john@example.org", "plain", true, null);

        User updated = original.withPassword("hashed");

        assertThat(updated.password()).isEqualTo("hashed");
        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.userName()).isEqualTo(original.userName());
    }

    @Test
    void withUserAccessRights_returnsCopyWithOnlyAccessRightsChanged() {
        User original = new User(1, "First", "Last", "john", "john@example.org", "hashed", true, null);
        UserAccessRights rights = new UserAccessRights(1, true, true, false, false);

        User updated = original.withUserAccessRights(rights);

        assertThat(updated.userAccessRights()).isEqualTo(rights);
        assertThat(updated.password()).isEqualTo(original.password());
    }

    @Test
    void equals_isBasedOnUsernameOnly() {
        User a = new User(1, "First", "Last", "john", "a@example.org", "pw1", true, null);
        User b = new User(2, "Different", "Name", "john", "b@example.org", "pw2", false, null);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void equals_differentUsername_notEqual() {
        User a = new User(1, "First", "Last", "john", "a@example.org", "pw", true, null);
        User b = new User(1, "First", "Last", "jane", "a@example.org", "pw", true, null);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void hashCode_nullUsername_doesNotThrow() {
        User user = new User(1, "First", "Last", null, "a@example.org", "pw", true, null);

        assertThat(user.hashCode()).isZero();
    }
}
