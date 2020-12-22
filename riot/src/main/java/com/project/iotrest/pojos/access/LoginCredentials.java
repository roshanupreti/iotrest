package com.project.iotrest.pojos.access;

/**
 * Class to represent user identifier and password.
 *
 * @author roshan
 */
public class LoginCredentials {

    private String identifier;

    private String password;

    public LoginCredentials() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
