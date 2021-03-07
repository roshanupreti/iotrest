package com.project.iotrest.access.providers;

import com.project.iotrest.access.annotation.RequiredAccessType;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.Access;
import com.project.iotrest.service.token.JWTProvider;
import io.jsonwebtoken.Claims;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.project.iotrest.exceptions.ErrorStatusCodes.UNAUTHORIZED;


/**
 * Provider Class for authentication filter.
 *
 * @author roshan
 */
@RequiredAccessType
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Inject
    private JWTProvider jwtProvider;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            throw new RESTException(UNAUTHORIZED.getCode(), "Missing Authorization Header.");
        }
        String token = authorizationHeader.substring("Bearer".length()).trim();
        Claims claims = jwtProvider.parseAndValidateToken(token);
        if (claims == null) {
            throw new RESTException(UNAUTHORIZED.getCode(), "Invalid token");
        }
        Object accessRights = claims.getOrDefault("access-levels", ArrayList.class);
        final String particleToken = String.valueOf(claims.getOrDefault("particle-token", String.class));
        List<String> accessList = null;
        if (accessRights instanceof List) {
            accessList = (List<String>) accessRights;
        }
        modifyRequestContext(requestContext, claims.getSubject(), getAccessRightsSet(accessList));
        requestContext.setProperty("particle-access-token", particleToken);
    }

    /**
     * Modify {@link ContainerRequestContext} based on the user login.
     *
     * @param requestContext {@link ContainerRequestContext}
     * @param user           String
     * @param accessRights   String
     */
    private void modifyRequestContext(ContainerRequestContext requestContext,
                                      final String user,
                                      Set<Access> accessRights) {
        final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
        requestContext.setSecurityContext(new SecurityContext() {

            @Override
            public Principal getUserPrincipal() {
                return () -> user;
            }

            @Override
            public boolean isUserInRole(String role) {
                return accessRights.contains(Access.valueOf(role));
            }

            @Override
            public boolean isSecure() {
                return currentSecurityContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        });
    }

    /**
     * Converts a list of String to a Set of {@link Access}
     *
     * @param accessList List
     * @return Set
     */
    private Set<Access> getAccessRightsSet(List<String> accessList) {
        return accessList.stream()
                .map(Access::fromString)
                .collect(Collectors.toSet());
    }
}
