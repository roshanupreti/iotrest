package com.project.iotrest.exceptions;

public class ApplicationException extends Exception {

    private final Integer statusCode;

    private final String responseMessage;

    public ApplicationException(Integer statusCode, String responseMessage) {
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }
}
