package com.project.iotrest.access.providers;


import com.project.iotrest.access.annotation.RequiredAccessType;
import com.project.iotrest.exceptions.ErrorStatusCodes;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.Access;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * Provider Class for authorization filter.
 *
 * @author roshan
 */
@RequiredAccessType
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        // Get class containing the ordered URL and extract it's permission level
        Class<?> clazz = resourceInfo.getResourceClass();
        Access[] accesses = extractPermissionLevel(clazz);

        // Get the method containing the requested URL and extract it's permission level
        Method method = resourceInfo.getResourceMethod();
        Access[] accessMethod = extractPermissionLevel(method);


        // How to modify the securityContext at the time of validating the token, so we can get The user login, to verify if it has the required permission level for this endpoint
        //String login = requestContext.getSecurityContext().getUserPrincipal().getName();
        //User user = (User) requestContext.getSecurityContext().getUserPrincipal();

        //  Checks if the user has permission to execute this method. The access levels of the method overlapping the class
        if (accessMethod.length == 0) {
            checkPermission(accesses, requestContext);
        } else {
            checkPermission(accessMethod, requestContext);
        }
    }

    // Method that extracts the permission levels that have been set in @Accesslevel
    private Access[] extractPermissionLevel(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return new Access[0];
        } else {
            RequiredAccessType requiredAccessType = annotatedElement.getAnnotation(RequiredAccessType.class);
            if (requiredAccessType == null) {
                return new Access[0];
            } else {
                return requiredAccessType.value();
            }
        }
    }

    /**
     * Checks if the user has permission to execute the method.
     *
     * @param permittedAccess ArrayList
     * @param requestContext {@link ContainerRequestContext}
     */
    private void checkPermission(Access[] permittedAccess, ContainerRequestContext requestContext) {
        if (permittedAccess.length == 0) {
            return;
        }
        boolean isPermitted = false;
        for (Access access : permittedAccess) {
            if (requestContext.getSecurityContext().isUserInRole(access.name())) {
                isPermitted = true;
                break;
            }
        }
        if (!isPermitted) {
            throw new RESTException(ErrorStatusCodes.FORBIDDEN.getCode(), "You do not have enough privileges to perform this action.");
        }
    }
}
