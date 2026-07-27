package com.project.iotrest.exceptions.wrapper;

import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * Wrapper class used by {@link com.project.iotrest.exceptions.RESTException} mapper to send as response when runtime
 * exceptions are thrown.
 *
 * @author roshan
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Ignore null fields
public record ErrorWrapper(Integer code, String status, String reason, String action, String apiPath) {
}
