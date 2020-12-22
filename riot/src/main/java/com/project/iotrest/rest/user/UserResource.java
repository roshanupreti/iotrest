package com.project.iotrest.rest.user;

import com.project.iotrest.access.annotation.Accessible;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.Access;
import com.project.iotrest.pojos.user.User;
import com.project.iotrest.service.user.UserService;
import com.project.iotrest.validation.RequestParamValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource class for {@link User}
 *
 * @author roshan
 */

@Path("users")
@Api("users")
@RequestScoped
public class UserResource {

    @Inject
    private UserService userService;

    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request."),
            @ApiResponse(code = 401, message = "Unauthorized Access."),
            @ApiResponse(code = 404, message = "Resource Not Found."),
            @ApiResponse(code = 403, message = "Access Forbidden"),
            @ApiResponse(code = 500, message = "Server Error.")
    })

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Accessible(Access.CREATE)
    @ApiOperation(
            value = "Creates a new user.",
            response = User.class
    )
    public User createUser(User user) {
        try {
            RequestParamValidator.validatePost(user);
            return userService.createUser(user);
        } catch (ApplicationException e) {
            throw new RESTException(e.getStatusCode(), e.getResponseMessage());
        }
    }

    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Accessible(Access.READ)
    @ApiOperation(
            value = "Gets a user by either username or e-mail.",
            response = User.class
    )
    public User getUser(@QueryParam("nameOrEmail") String nameOrEmail) {
        RequestParamValidator.validateString(nameOrEmail);
        try {
            return userService.getUserByUserNameOrEmail(nameOrEmail);
        } catch (ApplicationException e) {
            throw new RESTException(e.getStatusCode(), e.getResponseMessage());
        }
    }*/

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Accessible(Access.READ)
    @ApiOperation(
            value = "Gets a user by id.",
            response = User.class
    )
    public User getUserById(@PathParam("id") Integer id) {
        RequestParamValidator.validateId(id);
        try {
            return userService.getUserById(id);
        } catch (ApplicationException e) {
            throw new RESTException(e.getStatusCode(), e.getResponseMessage());
        }
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Accessible(Access.DELETE)
    @ApiOperation(
            value = "Deletes a user by id.",
            response = Response.class
    )
    public Response deleteUserById(@PathParam("id") Integer userId) {
        RequestParamValidator.validateId(userId);
        int deleteStatus = userService.deleteUser(userId);
        if (deleteStatus == 1) {
            return Response.ok().build();
        }
        return Response.serverError().build();
    }
}
