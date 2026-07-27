package com.project.iotrest.rest.devices;

import com.project.iotrest.access.annotation.RequiredAccessType;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.Access;
import com.project.iotrest.service.devices.DeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author roshan
 */
@Path("devices")
@Api("devices")
public class DeviceResource {

    private final DeviceService deviceService;

    @Inject
    public DeviceResource(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request."),
            @ApiResponse(code = 401, message = "Unauthorized Access."),
            @ApiResponse(code = 404, message = "Resource Not Found."),
            @ApiResponse(code = 403, message = "Access Forbidden"),
            @ApiResponse(code = 500, message = "Server Error.")
    })

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequiredAccessType(Access.READ)
    @ApiOperation(
            value = "List all available devices.",
            response = Response.class
    )
    public Response getDevices() {
        try {
            return Response.ok(deviceService.getDevices()).build();
        } catch (ApplicationException e) {
            throw new RESTException(e.getStatusCode(), e.getResponseMessage());
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiredAccessType(Access.READ)
    @ApiOperation(
            value = "Return a device, based on the id.",
            response = Response.class
    )
    public Response getDeviceById(@PathParam("id") String deviceId) {
        try {
            return Response.ok(deviceService.getDeviceInfo(deviceId)).build();
        } catch (ApplicationException e) {
            throw new RESTException(e.getStatusCode(), e.getResponseMessage());
        }
    }

    @GET
    @Path("{id}/variables/{variableName}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiredAccessType(Access.READ)
    @ApiOperation(
            value = "Reads the current value of a variable exposed by the device's firmware.",
            response = Response.class
    )
    public Response getVariableValue(@PathParam("id") String deviceId,
                                     @PathParam("variableName") String variableName) {
        try {
            return Response.ok(deviceService.getVariableValue(deviceId, variableName)).build();
        } catch (ApplicationException e) {
            throw new RESTException(e.getStatusCode(), e.getResponseMessage());
        }
    }

    @POST
    @Path("{id}/functions/{functionName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiredAccessType(Access.UPDATE)
    @ApiOperation(
            value = "Invokes a function exposed by the device's firmware, passing an optional string argument.",
            response = Response.class
    )
    public Response callFunction(@PathParam("id") String deviceId,
                                 @PathParam("functionName") String functionName,
                                 String arg) {
        try {
            return Response.ok(deviceService.callFunction(deviceId, functionName, arg)).build();
        } catch (ApplicationException e) {
            throw new RESTException(e.getStatusCode(), e.getResponseMessage());
        }
    }
}
