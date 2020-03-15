package com.project.iotrest.rest.user;

import com.project.iotrest.access.annotation.Accessible;
import com.project.iotrest.pojos.Access;
import com.project.iotrest.pojos.User;
import com.project.iotrest.service.user.UserService;
import com.project.iotrest.validation.RequestParamValidator;

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
@RequestScoped
public class UserResource {

    @Inject
    private UserService userService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Accessible(Access.CREATE)
    public User createUser(User user) {
        RequestParamValidator.validatePost(user);
        return userService.createUser(user);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Accessible(Access.READ)
    public User getUser(@QueryParam("nameOrEmail") String nameOrEmail) {
        RequestParamValidator.validateString(nameOrEmail);
        return userService.getUserByUserNameOrEmail(nameOrEmail);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Accessible(Access.READ)
    public User getUserById(@PathParam("id") Integer id) {
        RequestParamValidator.validateId(id);
       return userService.getUserById(id);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Accessible(Access.DELETE)
    public Response deleteUserById(@PathParam("id") Integer userId) {
        RequestParamValidator.validateId(userId);
        int deleteStatus = userService.deleteUser(userId);
        if (deleteStatus == 1) {
            return Response.ok().build();
        }
        return Response.serverError().build();
    }
}
