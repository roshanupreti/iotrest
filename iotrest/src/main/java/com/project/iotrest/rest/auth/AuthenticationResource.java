package com.project.iotrest.rest.auth;

import com.project.iotrest.service.token.TokenService;
import com.project.iotrest.pojos.LoginCredentials;
import com.project.iotrest.pojos.User;
import com.project.iotrest.service.user.UserService;
import com.project.iotrest.validation.CredentialsValidator;

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
@RequestScoped
public class AuthenticationResource {

    @Inject
    private UserService userService;

    @Inject
    private TokenService tokenService;

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticate(LoginCredentials loginCredentials) {
        CredentialsValidator.validateLoginCredentials(loginCredentials);
        User user = userService.getUserByUserNameOrEmail(loginCredentials.getIdentifier());
        return Response.ok(tokenService.generateToken(user)).build();
    }

    //todo: implement an endpoint that revokes the JWT.
}
