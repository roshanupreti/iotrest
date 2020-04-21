package com.project.iotrest.validation;

import com.project.iotrest.exceptions.ErrorStatusCodes;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.LoginCredentials;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

/**
 * A simple static class to perform login credentials validation.
 *
 * @author roshan
 */
public class CredentialsValidator {

    private static final String BAD_CREDENTIALS_MSG = "Missing both user identifier and password.";
    private static final String NO_IDENTIFIER_MSG = "Missing user identifier. Enter username or e-mail.";
    private static final String NO_PWD_MSG = "Missing password. Enter the correct password.";

    private CredentialsValidator() {
        throw new UnsupportedOperationException();
    }

    public static void validateLoginCredentials(LoginCredentials loginCredentials) {
        StringBuilder message = new StringBuilder();
        if (loginCredentials == null) {
            message.append(BAD_CREDENTIALS_MSG);
        } else {
            if (StringUtils.isEmpty(loginCredentials.getIdentifier())) {
                message.append(NO_IDENTIFIER_MSG);
            }
            if (StringUtils.isEmpty(loginCredentials.getPassword())) {
                message.append(NO_PWD_MSG);
            }
        }
        if (!StringUtils.isEmpty(message.toString())
                && message.toString().length() > 1) {
            throw new RESTException(ErrorStatusCodes.BAD_REQUEST.getCode(), message.toString());
        }
    }

    public static String hashPassword(String plainTextPasswordString) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(plainTextPasswordString, salt);
    }

    public static Boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (StringUtils.isEmpty(hashedPassword)
                || !StringUtils.startsWith(hashedPassword, "$2a$")) {
            throw new RuntimeException("Invalid hash");
        }
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }

    private static void validatePassword(String hashedPass, String plainTextPass) throws Exception {
        if (BooleanUtils.isFalse(checkPassword(plainTextPass, hashedPass))) {
            throw new Exception("Incorrect password.");
        }
    }
}
