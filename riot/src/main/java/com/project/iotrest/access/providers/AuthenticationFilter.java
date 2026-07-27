package com.project.iotrest.access.providers;

import com.project.iotrest.access.annotation.RequiredAccessType;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.Access;
import com.project.iotrest.security.JWTUserPrincipal;
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

    private final JWTProvider jwtProvider;

    @Inject
    public AuthenticationFilter(JWTProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

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
        Object accessRights = claims.getOrDefault("access-levels", new ArrayList<>());
        List<String> accessList = null;
        if (accessRights instanceof List) {
            accessList = (List<String>) accessRights;
        }

        Set<Access> accessRightsSet = getAccessRightsSet(accessList);

        String username = claims.getSubject();
        Integer userId = claims.get("id", Integer.class);
        String email = claims.get("email", String.class);

        JWTUserPrincipal principal = new JWTUserPrincipal(userId, username, email, accessRightsSet);

        modifyRequestContext(requestContext, principal, accessRightsSet);
    }

    /**
     * Modify {@link ContainerRequestContext} based on the user login.
     *
     * @param requestContext {@link ContainerRequestContext}
     * @param principal      Principal for the authenticated user
     * @param accessRights   Set of access rights
     */
    private void modifyRequestContext(ContainerRequestContext requestContext,
                                      final Principal principal,
                                      Set<Access> accessRights) {
        final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
        requestContext.setSecurityContext(new SecurityContext() {

            @Override
            public Principal getUserPrincipal() {
                return principal;
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
