package com.project.iotrest.rest.device;

import com.project.iotrest.access.annotation.RequiredAccessType;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.Access;
import com.project.iotrest.service.devices.DeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;

/**
 * @author roshan
 */
@Path("devices")
@Api("devices")
@RequestScoped
public class DeviceResource {

    @Context
    private ContainerRequestContext containerRequestContext;


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
    public Response getDevices() throws MalformedURLException {
        try {
            return Response.ok(new DeviceService(containerRequestContext.getProperty("particle-access-token").toString())
                    .getDevices()).build();
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
    public Response getDeviceById(@PathParam("id") String deviceId) throws MalformedURLException {
        try {
            return Response.ok(new DeviceService(containerRequestContext.getProperty("particle-access-token").toString())
            .getDeviceInfo(deviceId)).build();
        } catch (ApplicationException e) {
            throw new RESTException(e.getStatusCode(), e.getResponseMessage());
        }
    }
}
