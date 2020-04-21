package com.project.iotrest.rest.auth;

import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.LoginCredentials;
import com.project.iotrest.pojos.User;
import com.project.iotrest.service.token.TokenService;
import com.project.iotrest.service.user.UserService;
import com.project.iotrest.validation.CredentialsValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Authentication resource class. Invoking `login` with valid credentials returns a valid JWT.
 *
 * @author roshan
 */
@Path("auth")
@Api("auth")
@RequestScoped
public class AuthenticationResource {

    @Inject
    private UserService userService;

    @Inject
    private TokenService tokenService;

    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request."),
            @ApiResponse(code = 401, message = "Unauthorized Access."),
            @ApiResponse(code = 404, message = "Resource Not Found."),
            @ApiResponse(code = 403, message = "Access Forbidden"),
            @ApiResponse(code = 500, message = "Server Error.")
    })


    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Authenticates a user against the provided credentials",
            response = Response.class
    )
    public Response authenticate(LoginCredentials loginCredentials) {
        try {
            CredentialsValidator.validateLoginCredentials(loginCredentials);
            User user = userService.getUserByUserNameOrEmail(loginCredentials.getIdentifier());
            return Response.ok(tokenService.generateToken(user)).build();
        } catch (ApplicationException e) {
            throw new RESTException(e.getStatusCode(), e.getResponseMessage());
        }
    }

    //todo: implement an endpoint that revokes the JWT.
}
