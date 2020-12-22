package com.project.iotrest.pojos.access;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *This class holds the parameters, based on which a JWT is issued.
 */
public class AuthenticationTokenParams {

    private final String id;
    private final String username;
    private final String email;
    private final Set<Access> accessRights;
    private final ZonedDateTime issuedDate;
    private final ZonedDateTime expirationDate;

    private AuthenticationTokenParams(String id, String username, String email, Set<Access> accessRights,
                                      ZonedDateTime issuedDate, ZonedDateTime expirationDate) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.accessRights = accessRights;
        this.issuedDate = issuedDate;
        this.expirationDate = expirationDate;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Set<Access> getAccessRights() {
        return accessRights;
    }

    public ZonedDateTime getIssuedDate() {
        return issuedDate;
    }

    public ZonedDateTime getExpirationDate() {
        return expirationDate;
    }

    public static class Builder {

        private String id;
        private String username;
        private String email;
        private Set<Access> accessRights;
        private ZonedDateTime issuedDate;
        private ZonedDateTime expirationDate;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withAccessRights(Set<Access> accessRights) {
            this.accessRights = Collections.unmodifiableSet(accessRights == null ? new HashSet<>() : accessRights);
            return this;
        }

        public Builder withIssuedDate(ZonedDateTime issuedDate) {
            this.issuedDate = issuedDate;
            return this;
        }

        public Builder withExpirationDate(ZonedDateTime expirationDate) {
            this.expirationDate = expirationDate;
            return this;
        }

        public AuthenticationTokenParams build() {
            return new AuthenticationTokenParams(id, username, email, accessRights, issuedDate, expirationDate);
        }
    }
}
