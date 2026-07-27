package com.project.iotrest.rest.user;

import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.user.User;
import com.project.iotrest.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserResourceTest {

    @Mock
    private UserService userService;

    private UserResource resource;

    @BeforeEach
    void setUp() {
        resource = new UserResource(userService);
    }

    private static User rawUser(String userName, String email) {
        return new User(null, "First", "Last", userName, email, "secret", true, null);
    }

    @Test
    void createUser_happyPath_returnsServiceResult() throws ApplicationException {
        User created = new User(1, "First", "Last", "john", "john@example.org", "hashed", true, null);
        when(userService.createUser(rawUser("john", "john@example.org"))).thenReturn(created);

        User result = resource.createUser(rawUser("john", "john@example.org"));

        assertThat(result).isEqualTo(created);
    }

    @Test
    void createUser_missingUsername_rejectedBeforeReachingService() {
        assertThatThrownBy(() -> resource.createUser(rawUser("", "john@example.org")))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getStatusCode()).isEqualTo(400));

        verifyNoInteractions(userService);
    }

    @Test
    void createUser_serviceThrows_propagatesAsRestException() throws ApplicationException {
        when(userService.createUser(rawUser("john", "john@example.org")))
                .thenThrow(new ApplicationException(400, "User already exists."));

        assertThatThrownBy(() -> resource.createUser(rawUser("john", "john@example.org")))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getResponseMessage()).isEqualTo("User already exists."));
    }

    @Test
    void getUserById_happyPath_returnsServiceResult() throws ApplicationException {
        User user = new User(1, "First", "Last", "john", "john@example.org", "hashed", true, null);
        when(userService.getUserById(1)).thenReturn(user);

        assertThat(resource.getUserById(1)).isEqualTo(user);
    }

    @Test
    void getUserById_invalidId_rejectedBeforeReachingService() {
        assertThatThrownBy(() -> resource.getUserById(0))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getStatusCode()).isEqualTo(400));

        verifyNoInteractions(userService);
    }

    @Test
    void getUserById_serviceThrows_propagatesAsRestException() throws ApplicationException {
        when(userService.getUserById(999)).thenThrow(new ApplicationException(404, "User not found with id 999"));

        assertThatThrownBy(() -> resource.getUserById(999))
                .isInstanceOf(RESTException.class)
                .satisfies(e -> assertThat(((RESTException) e).getStatusCode()).isEqualTo(404));
    }

    @Test
    void deleteUserById_successfulDeletion_returnsOk() {
        when(userService.deleteUser(1)).thenReturn(1);

        Response response = resource.deleteUserById(1);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void deleteUserById_deletionDidNotAffectARow_returnsServerError() {
        when(userService.deleteUser(1)).thenReturn(0);

        Response response = resource.deleteUserById(1);

        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    void deleteUserById_invalidId_rejectedBeforeReachingService() {
        assertThatThrownBy(() -> resource.deleteUserById(null))
                .isInstanceOf(RESTException.class);

        verifyNoInteractions(userService);
    }
}
