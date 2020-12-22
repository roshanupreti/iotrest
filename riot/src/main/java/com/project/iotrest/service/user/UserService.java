package com.project.iotrest.service.user;


import com.project.iotrest.dao.user.UserDao;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.pojos.access.LoginCredentials;
import com.project.iotrest.pojos.user.User;
import com.project.iotrest.rest.user.UserResource;
import com.project.iotrest.validation.CredentialsValidator;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import static com.project.iotrest.exceptions.ErrorStatusCodes.BAD_REQUEST;

/**
 * Service class for {@link UserResource}
 *
 * @author roshan
 */
@Dependent
public class UserService {

    @Inject
    private UserDao userDao;

    public User createUser(User user) throws ApplicationException {
        /* Check if the user already exists. */
        if (userExists(user.getUserName())
                || userExists(user.getEmail())) {
            throw new ApplicationException(BAD_REQUEST.getCode(), "User already exists.");
        }
        /* Hash password before creating user. */
        user.setPassword(CredentialsValidator.hashPassword(user.getPassword()));
        return userDao.createUser(user);
    }

    public User getUserById(Integer id) throws ApplicationException {
        return userDao.getUserById(id);
    }

    public User attemptUserLogin(LoginCredentials loginCredentials) throws ApplicationException {
        User user = userDao.getUserByUserNameOrEmail(loginCredentials.getIdentifier());
        if (BCrypt.checkpw(loginCredentials.getPassword(), user.getPassword())) {
            return user;
        }
        throw new ApplicationException(BAD_REQUEST.getCode(), "Incorrect Password.");
    }

    public Boolean userExists(String queryParam) {
        return userDao.userExists(queryParam);
    }

    public int deleteUser(Integer userId) {
        return userDao.deleteUser(userId);
    }
}
