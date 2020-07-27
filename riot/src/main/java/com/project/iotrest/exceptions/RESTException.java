package com.project.iotrest.exceptions;


import com.project.iotrest.exceptions.wrapper.ErrorWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static java.lang.String.*;

/**
 * A RuntimeException wrapper provider class.
 *
 * @author roshan
 */
@Provider
public class RESTException extends RuntimeException implements ExceptionMapper<RESTException> {

    private static final long serialVersionUID = 1L;

    private Integer statusCode;

    private String responseMessage;

    public RESTException() {
        /*Absence of an empty constructor will lead to the failure of instance creation during the runtime.*/
    }

    public RESTException(Integer statusCode, String responseMessage) {
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
    }

    /*  transient -> not to be serialized when persisted to streams of bytes */
    @Context
    private transient UriInfo uriInfo;

    @Context
    private transient HttpServletRequest request;

    public Integer getStatusCode() {
        return this.statusCode;
    }

    public String getResponseMessage() {
        return this.responseMessage;
    }

    /**
     * Whenever {@link RESTException} is thrown, the server returns a response, with ErrorWrapper as the entity that's
     * created based on the exception parameters.
     * */
    @Override
    public Response toResponse(RESTException exception) {
        Status status = Status.fromStatusCode(exception.getStatusCode());
        ErrorWrapper errorWrapper = new ErrorWrapper();
        errorWrapper.setCode(status.getStatusCode());
        errorWrapper.setStatus(status.getReasonPhrase());
        errorWrapper.setReason(exception.getResponseMessage());
        errorWrapper.setAction(request.getMethod());
        errorWrapper.setApiPath(format("%s%s", request.getServletPath(), request.getPathInfo()));
        return Response.status(status).entity(errorWrapper).type(MediaType.APPLICATION_JSON).build();
    }
}
