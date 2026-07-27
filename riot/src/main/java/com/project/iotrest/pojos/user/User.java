package com.project.iotrest.pojos.user;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * POJO for `users` table.
 *
 * @author roshan
 */
@Table(name = "USERS")
public record User(@Column(name = "ID") Integer id,
                   @Column(name = "FIRST_NAME") String firstName,
                   @Column(name = "LAST_NAME") String lastName,
                   @Column(name = "USERNAME") String userName,
                   @Column(name = "EMAIL") String email,
                   @Column(name = "PASSWORD") String password,
                   @Column(name = "IS_ACTIVE") Boolean active,
                   UserAccessRights userAccessRights) {

    public static User empty() {
        return new User(null, null, null, null, null, null, null, null);
    }

    public User withPassword(String password) {
        return new User(id, firstName, lastName, userName, email, password, active, userAccessRights);
    }

    public User withUserAccessRights(UserAccessRights userAccessRights) {
        return new User(id, firstName, lastName, userName, email, password, active, userAccessRights);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {
        return userName != null ? userName.hashCode() : 0;
    }
}
