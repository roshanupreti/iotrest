package com.project.iotrest.service.user;


import com.project.iotrest.dao.user.UserDao;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.User;
import com.project.iotrest.rest.user.UserResource;
import com.project.iotrest.validation.CredentialsValidator;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import static com.project.iotrest.exceptions.ErrorStatusCodes.BAD_REQUEST;

/**
 * Service class for {@link UserResource}
 * @author roshan
 */
@Dependent
public class UserService {

    @Inject
    private UserDao userDao;

    public User createUser(User user) {
        /* Check if the user already exists. */
        if (userExists(user.getUserName())
        || userExists(user.getEmail())) {
            throw new RESTException(BAD_REQUEST.getCode(), "User already exists.");
        }
        /* Hash password before creating user. */
        user.setPassword(CredentialsValidator.hashPassword(user.getPassword()));
        return userDao.createUser(user);
    }

    public User getUserById(Integer id) {
        return userDao.getUserById(id);
    }

    public User getUserByUserNameOrEmail(String queryParam) {
        return userDao.getUserByUserNameOrEmail(queryParam);
    }

    public Boolean userExists(String queryParam) {
        return userDao.userExists(queryParam);
    }

    public int deleteUser(Integer userId) {
        return userDao.deleteUser(userId);
    }
}
