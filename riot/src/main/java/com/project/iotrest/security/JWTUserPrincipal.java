package com.project.iotrest.security;

import com.project.iotrest.pojos.access.Access;
import lombok.Getter;

import java.security.Principal;
import java.util.Objects;
import java.util.Set;

/**
 * Principal for a JWT-authenticated user, built from the token's claims.
 *
 * @author roshan
 */
@Getter
public class JWTUserPrincipal implements Principal {

    private final Integer id;
    private final String username;
    private final String email;
    private final Set<Access> accessLevels;

    public JWTUserPrincipal(
            Integer id,
            String username,
            String email,
            Set<Access> accessLevels) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.accessLevels = accessLevels;
    }

    @Override
    public String getName() {
        return username;
    }

    public boolean hasAccess(String accessLevel) {
        try {
            Access access = Access.valueOf(accessLevel);
            return accessLevels != null && accessLevels.contains(access);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JWTUserPrincipal that = (JWTUserPrincipal) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "JWTUserPrincipal{" +
                "userId=" + id +
                ", username='" + username + '\'' +
                ", accessLevels=" + accessLevels +
                '}';
    }
}
