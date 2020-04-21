package com.project.iotrest.exceptions;

/**
 * Enum class to store possible error codes mapped to their respective titles.
 *
 * @author roshan
 */
public enum ErrorStatusCodes {

    NOT_FOUND(404),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    SERVER_ERROR(500);

    private int code;

    ErrorStatusCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
