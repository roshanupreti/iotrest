package com.project.iotrest.exceptions;

import lombok.Getter;

@Getter
public class ApplicationException extends Exception {

    private final Integer statusCode;

    private final String responseMessage;

    public ApplicationException(Integer statusCode, String responseMessage) {
        super(responseMessage);
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
    }
}
