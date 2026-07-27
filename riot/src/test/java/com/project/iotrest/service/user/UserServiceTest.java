package com.project.iotrest.service.user;

import com.project.iotrest.dao.UserDao;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.pojos.access.LoginCredentials;
import com.project.iotrest.pojos.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao);
    }

    private static User rawUser(String userName, String email, String password) {
        return new User(null, "First", "Last", userName, email, password, true, null);
    }

    @Test
    void createUser_usernameAlreadyExists_throwsWithoutCallingDao() throws ApplicationException {
        when(userDao.userExists("john")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(rawUser("john", "john@example.org", "secret")))
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> {
                    assertThat(((ApplicationException) e).getStatusCode()).isEqualTo(400);
                    assertThat(((ApplicationException) e).getResponseMessage()).isEqualTo("User already exists.");
                });
        verify(userDao, never()).createUser(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createUser_emailAlreadyExists_throwsWithoutCallingDao() throws ApplicationException {
        when(userDao.userExists("john")).thenReturn(false);
        when(userDao.userExists("john@example.org")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(rawUser("john", "john@example.org", "secret")))
                .isInstanceOf(ApplicationException.class);
        verify(userDao, never()).createUser(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createUser_newUser_hashesPasswordBeforePersisting() throws ApplicationException {
        when(userDao.userExists(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(userDao.createUser(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(rawUser("john", "john@example.org", "plaintext-secret"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).createUser(captor.capture());
        String persistedPassword = captor.getValue().password();
        assertThat(persistedPassword).isNotEqualTo("plaintext-secret");
        assertThat(BCrypt.checkpw("plaintext-secret", persistedPassword)).isTrue();
    }

    @Test
    void getUserById_delegatesToDao() throws ApplicationException {
        User expected = rawUser("john", "john@example.org", "hashed");
        when(userDao.getUserById(1)).thenReturn(expected);

        assertThat(userService.getUserById(1)).isEqualTo(expected);
    }

    @Test
    void getUserById_daoThrows_propagates() throws ApplicationException {
        when(userDao.getUserById(999)).thenThrow(new ApplicationException(404, "User not found with id 999"));

        assertThatThrownBy(() -> userService.getUserById(999))
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> assertThat(((ApplicationException) e).getStatusCode()).isEqualTo(404));
    }

    @Test
    void attemptUserLogin_correctPassword_returnsUser() throws ApplicationException {
        String hashed = BCrypt.hashpw("secret", BCrypt.gensalt());
        User stored = rawUser("john", "john@example.org", hashed);
        when(userDao.getUserByUserNameOrEmail("john")).thenReturn(stored);

        User result = userService.attemptUserLogin(new LoginCredentials("john", "secret"));

        assertThat(result).isEqualTo(stored);
    }

    @Test
    void attemptUserLogin_incorrectPassword_throwsBadRequest() throws ApplicationException {
        String hashed = BCrypt.hashpw("secret", BCrypt.gensalt());
        User stored = rawUser("john", "john@example.org", hashed);
        when(userDao.getUserByUserNameOrEmail("john")).thenReturn(stored);

        assertThatThrownBy(() -> userService.attemptUserLogin(new LoginCredentials("john", "wrong-password")))
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> {
                    assertThat(((ApplicationException) e).getStatusCode()).isEqualTo(400);
                    assertThat(((ApplicationException) e).getResponseMessage()).isEqualTo("Incorrect Password.");
                });
    }

    @Test
    void attemptUserLogin_userNotFound_propagatesDaoException() throws ApplicationException {
        when(userDao.getUserByUserNameOrEmail("ghost"))
                .thenThrow(new ApplicationException(404, "User not found with username and/or email ghost"));

        assertThatThrownBy(() -> userService.attemptUserLogin(new LoginCredentials("ghost", "secret")))
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> assertThat(((ApplicationException) e).getStatusCode()).isEqualTo(404));
    }

    @Test
    void userExists_delegatesToDao() {
        when(userDao.userExists("john")).thenReturn(true);

        assertThat(userService.userExists("john")).isTrue();
    }

    @Test
    void deleteUser_delegatesToDao() {
        when(userDao.deleteUser(1)).thenReturn(1);

        assertThat(userService.deleteUser(1)).isEqualTo(1);
    }
}
