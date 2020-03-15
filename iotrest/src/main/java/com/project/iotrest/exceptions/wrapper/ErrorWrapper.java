package com.project.iotrest.exceptions.wrapper;

import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * Wrapper class used by {@link com.project.iotrest.exceptions.RESTException} mapper to send as response when runtime
 * exceptions are thrown.
 *
 * @author roshan
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Ignore null fields
public class ErrorWrapper {

    private Integer code;

    private String status;

    private String reason;

    private String action;

    private String apiPath;

    public ErrorWrapper() {
        /* Empty constructor for Jackson. */
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }
}
