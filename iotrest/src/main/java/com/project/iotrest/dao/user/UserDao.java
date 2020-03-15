package com.project.iotrest.dao.user;


import com.project.iotrest.dao.queries.UserQueries;
import com.project.iotrest.dbutil.DSLContextFactory;
import com.project.iotrest.exceptions.ErrorStatusCodes;
import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.User;
import com.project.iotrest.pojos.UserAccessRights;
import jooq_generated.tables.records.UsersAccessRightsRecord;
import jooq_generated.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.jooq.InsertQuery;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import static com.project.iotrest.exceptions.ErrorStatusCodes.SERVER_ERROR;
import static jooq_generated.tables.UsersAccessRights.USERS_ACCESS_RIGHTS;

/**
 * Data access layer implementation class responsible for database related operation, and its methods are called by
 * the service layer.
 *
 * @author roshan
 */
@Dependent
public class UserDao {

    @Inject
    private UserQueries queries;

    /**
     * Create new User
     *
     * @param user {@link User}
     * @return {@link User}
     */
    public User createUser(User user) {
        try (DSLContext ctx = new DSLContextFactory().getDSLContext()) {
            User createdUser = performUserInsert(ctx, user);
            if (user.getUserAccessRights() != null) {
                // Insert Access Rights.
                UserAccessRights userAccessRights = insertUserAccessRights(ctx, new UserAccessRights(createdUser.getId(),
                        user.getUserAccessRights().getCreate(),
                        user.getUserAccessRights().getRead(),
                        user.getUserAccessRights().getUpdate(),
                        user.getUserAccessRights().getDelete()));
                setUserAccessRights(createdUser, userAccessRights);
            }
            return createdUser;
        }
    }

    /**
     * Find a user by id.
     *
     * @param id Integer
     * @return {@link User}
     */
    public User getUserById(Integer id) {
        try (DSLContext ctx = new DSLContextFactory().getDSLContext()) {
            UsersRecord usersRecord = queries.getUserById(ctx, id)
                    .fetchOne();
            User user = processUserRecord(id.toString(), usersRecord);
            UsersAccessRightsRecord usersAccessRightsRecord = getUserAccessRightsRecord(ctx, id);
            UserAccessRights userAccessRights = processUserAccessRights(id, usersAccessRightsRecord);
            setUserAccessRights(user, userAccessRights);
            return user;
        }
    }

    /**
     * Find a user by username or email.
     *
     * @param queryParam String
     * @return {@link User}
     */
    public User getUserByUserNameOrEmail(String queryParam) {
        try (DSLContext ctx = new DSLContextFactory().getDSLContext()) {
            UsersRecord userRecord = queries.getUserByNameOrEmail(ctx, queryParam)
                    .fetchOne();
            User user = processUserRecord(queryParam, userRecord);
            UsersAccessRightsRecord usersAccessRightsRecord = getUserAccessRightsRecord(ctx, user.getId());
            UserAccessRights userAccessRights = processUserAccessRights(user.getId(), usersAccessRightsRecord);
            setUserAccessRights(user, userAccessRights);
            return user;
        }
    }

    /**
     * Insert a new user.
     *
     * @param ctx {@link DSLContext ctx}
     * @param user {@link User}
     * @return {@link User}
     */
    private User performUserInsert(DSLContext ctx, User user) {
        try (InsertQuery<UsersRecord> insertQuery = queries.createUser(ctx, user)) {
            insertQuery.execute();
            UsersRecord usersRecord = insertQuery.getReturnedRecord();
            if (usersRecord == null) {
                throw new RESTException(SERVER_ERROR.getCode(), "Error creating user");
            }
            return usersRecord.into(User.class);
        }
    }

    /**
     * Insert access rights for a user.
     *
     * @param ctx {@link DSLContext}
     * @param userAccessRights {@link UserAccessRights}
     * @return {@link UserAccessRights}
     */
    private UserAccessRights insertUserAccessRights(DSLContext ctx, UserAccessRights userAccessRights) {
        try (InsertQuery<UsersAccessRightsRecord> insertQuery = queries.insertUserAccessRights(ctx, userAccessRights)) {
            int insertStatus = insertQuery.execute();
            if (insertStatus != 1) {
                throw new RESTException(SERVER_ERROR.getCode(), "Error creating user access rights.");
            }
            return ctx.selectFrom(USERS_ACCESS_RIGHTS)
                    .where(USERS_ACCESS_RIGHTS.USER_ID.eq(userAccessRights.getUserId()))
                    .fetchOne()
                    .into(UserAccessRights.class);
        }
    }

    /**
     * Delete a user, based on the provided id.
     *
     * @param userId Integer
     * @return int
     */
    public int deleteUser(Integer userId) {
        try (DSLContext ctx = new DSLContextFactory().getDSLContext()) {
            return queries.userDeleteQuery(ctx, userId).execute();
        }
    }

    /**
     * Checks if the user with the provided identifier exists.
     *
     * @param usernameOrEmail String
     * @return Boolean
     */
    public Boolean userExists(String usernameOrEmail) {
        try (DSLContext ctx = new DSLContextFactory().getDSLContext()) {
            return ctx.fetchExists(
                    queries.getUserByNameOrEmail(ctx, usernameOrEmail)
            );
        }
    }

    /**
     * Get access rights for the user with the provided id.
     *
     * @param ctx {@link DSLContext}
     * @param id Integer
     * @return {@link UserAccessRights}
     */
    private UsersAccessRightsRecord getUserAccessRightsRecord(DSLContext ctx, Integer id) {
        return queries.getUserAccessRights(ctx, id).fetchOne();
    }

    /**
     * Validates the provided user record.
     *
     * @param queryParam
     * @param usersRecord
     * @return
     */
    private User processUserRecord(String queryParam, UsersRecord usersRecord) {
        if (usersRecord == null) {
            StringBuilder message = new StringBuilder();
            if (queryParam.matches("-?(0|[1-9]\\d*)")) {
                message.append("User id ").append(queryParam).append(" not found");
            } else {
                message.append("Username ").append(queryParam).append(" not found");
            }
            throw new RESTException(ErrorStatusCodes.NOT_FOUND.getCode(), message.toString());
        }
        return usersRecord.into(User.class);
    }

    /**
     * Validates the given user access rights record and lodge it into UserAccessRights Pojo.
     *
     * @param id Integer
     * @param usersAccessRightsRecord {@link UsersAccessRightsRecord}
     * @return {@link UserAccessRights}
     */
    private UserAccessRights processUserAccessRights(Integer id, UsersAccessRightsRecord usersAccessRightsRecord) {
        if (usersAccessRightsRecord == null) {
            throw new RESTException(ErrorStatusCodes.NOT_FOUND.getCode(), "Access rights not found for user id " + id);
        }
        return usersAccessRightsRecord.into(UserAccessRights.class);
    }

    /**
     * Sets the provided access rights in the provided user.
     *
     * @param user
     * @param userAccessRights
     */
    private void setUserAccessRights(User user, UserAccessRights userAccessRights) {
        if (userAccessRights != null) {
            user.setUserAccessRights(userAccessRights);
        }
    }
}
