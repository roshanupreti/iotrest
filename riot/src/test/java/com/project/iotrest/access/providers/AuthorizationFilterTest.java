package com.project.iotrest.access.providers;

import com.project.iotrest.access.annotation.RequiredAccessType;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.Access;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationFilterTest {

    @RequiredAccessType(Access.READ)
    static class ClassLevelOnly {
        public void unannotatedMethod() {
        }
    }

    static class MethodLevelOnly {
        @RequiredAccessType(Access.DELETE)
        public void protectedMethod() {
        }
    }

    @RequiredAccessType(Access.READ)
    static class MethodOverridesClass {
        @RequiredAccessType(Access.UPDATE)
        public void protectedMethod() {
        }
    }

    static class NoAnnotationAnywhere {
        public void openMethod() {
        }
    }

    @RequiredAccessType({Access.READ, Access.UPDATE})
    static class MultipleAccessLevels {
        public void unannotatedMethod() {
        }
    }

    @Mock
    private ResourceInfo resourceInfo;

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private SecurityContext securityContext;

    private AuthorizationFilter filter;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        filter = new AuthorizationFilter();
        setResourceInfoField();
    }

    private void setResourceInfoField() throws NoSuchMethodException {
        try {
            var field = AuthorizationFilter.class.getDeclaredField("resourceInfo");
            field.setAccessible(true);
            field.set(filter, resourceInfo);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not inject mocked ResourceInfo via reflection", e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void givenResource(Class resourceClass, String methodName) throws NoSuchMethodException {
        Method method = resourceClass.getMethod(methodName);
        when(resourceInfo.getResourceClass()).thenReturn(resourceClass);
        when(resourceInfo.getResourceMethod()).thenReturn(method);
    }

    @Test
    void filter_methodHasNoAnnotation_fallsBackToClassLevel_permitted() throws NoSuchMethodException {
        givenResource(ClassLevelOnly.class, "unannotatedMethod");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.isUserInRole("READ")).thenReturn(true);

        assertThatCode(() -> filter.filter(requestContext)).doesNotThrowAnyException();
    }

    @Test
    void filter_methodHasNoAnnotation_fallsBackToClassLevel_forbidden() throws NoSuchMethodException {
        givenResource(ClassLevelOnly.class, "unannotatedMethod");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.isUserInRole("READ")).thenReturn(false);

        assertThatThrownBy(() -> filter.filter(requestContext))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getStatusCode()).isEqualTo(403));
    }

    @Test
    void filter_classHasNoAnnotation_usesMethodLevel_permitted() throws NoSuchMethodException {
        givenResource(MethodLevelOnly.class, "protectedMethod");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.isUserInRole("DELETE")).thenReturn(true);

        assertThatCode(() -> filter.filter(requestContext)).doesNotThrowAnyException();
    }

    @Test
    void filter_methodAnnotationOverridesClassAnnotation() throws NoSuchMethodException {
        givenResource(MethodOverridesClass.class, "protectedMethod");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        // User has the class-level (READ) role but NOT the method-level (UPDATE) role required.
        when(securityContext.isUserInRole("UPDATE")).thenReturn(false);

        assertThatThrownBy(() -> filter.filter(requestContext))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getStatusCode()).isEqualTo(403));
    }

    @Test
    void filter_noAnnotationAtAll_skipsAuthorizationEntirely() throws NoSuchMethodException {
        givenResource(NoAnnotationAnywhere.class, "openMethod");

        assertThatCode(() -> filter.filter(requestContext)).doesNotThrowAnyException();
        verifyNoInteractions(securityContext);
    }

    @Test
    void filter_multipleRequiredLevels_userHasOneOfThem_permitted() throws NoSuchMethodException {
        givenResource(MultipleAccessLevels.class, "unannotatedMethod");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.isUserInRole("READ")).thenReturn(false);
        when(securityContext.isUserInRole("UPDATE")).thenReturn(true);

        assertThatCode(() -> filter.filter(requestContext)).doesNotThrowAnyException();
    }

    @Test
    void filter_multipleRequiredLevels_userHasNoneOfThem_forbidden() throws NoSuchMethodException {
        givenResource(MultipleAccessLevels.class, "unannotatedMethod");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.isUserInRole("READ")).thenReturn(false);
        when(securityContext.isUserInRole("UPDATE")).thenReturn(false);

        assertThatThrownBy(() -> filter.filter(requestContext))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getResponseMessage())
                        .isEqualTo("You do not have enough privileges to perform this action."));
    }
}
