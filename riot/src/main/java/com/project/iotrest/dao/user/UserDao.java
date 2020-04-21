package com.project.iotrest.dao.user;


import com.project.iotrest.dao.queries.UserQueries;
import com.project.iotrest.dbutil.DSLContextFactory;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.ErrorStatusCodes;
import com.project.iotrest.pojos.User;
import com.project.iotrest.pojos.UserAccessRights;
import jooq_generated.tables.records.UsersAccessRightsRecord;
import jooq_generated.tables.records.UsersRecord;
import org.apache.commons.collections.MapUtils;
import org.jooq.DSLContext;
import org.jooq.InsertQuery;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.project.iotrest.exceptions.ErrorStatusCodes.SERVER_ERROR;
import static jooq_generated.tables.Users.USERS;
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
    public User createUser(User user) throws ApplicationException {
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
    public User getUserById(Integer id) throws ApplicationException {
        try (DSLContext ctx = new DSLContextFactory().getDSLContext()) {
            Map<User, List<UserAccessRights>> result = queries.getUserById(ctx, id)
                    .fetchGroups(
                            // Map records first into the USERS table and then into the key POJO type
                            r -> r.into(USERS).into(User.class),
                            // Map records first into the USERS_ACCESS_RIGHTS table and then into the value POJO type
                            r -> r.into(USERS_ACCESS_RIGHTS).into(UserAccessRights.class)
                    );
            return getUserAndAccessRights(new User(), result);
        }
    }

    /**
     * Find a user by username or email.
     *
     * @param queryParam String
     * @return {@link User}
     */
    public User getUserByUserNameOrEmail(String queryParam) throws ApplicationException {
        try (DSLContext ctx = new DSLContextFactory().getDSLContext()) {
            Map<User, List<UserAccessRights>> result = queries.getUserByNameOrEmail(ctx, queryParam)
                    .fetchGroups(
                            // Map records first into the USERS table and then into the key POJO type
                            r -> r.into(USERS).into(User.class),
                            // Map records first into the USERS_ACCESS_RIGHTS table and then into the value POJO type
                            r -> r.into(USERS_ACCESS_RIGHTS).into(UserAccessRights.class)
                    );
            return getUserAndAccessRights(new User(), result);
        }
    }

    private User getUserAndAccessRights(User user, Map<User, List<UserAccessRights>> result) throws ApplicationException {
        if (MapUtils.isNotEmpty(result)) {
            for (Map.Entry<User, List<UserAccessRights>> entry : result.entrySet()) {
                if (entry.getValue().size() <= 1) {
                    user = entry.getKey();
                    UserAccessRights userAccessRights = entry.getValue().get(0);
                    user.setUserAccessRights(userAccessRights);
                }
            }
        }
        if (user == null
                || user.getId() == null) {
            throw new ApplicationException(ErrorStatusCodes.NOT_FOUND.getCode(), "User not found.");
        }
        return user;
    }

    /**
     * Insert a new user.
     *
     * @param ctx  {@link DSLContext ctx}
     * @param user {@link User}
     * @return {@link User}
     */
    private User performUserInsert(DSLContext ctx, User user) throws ApplicationException {
        try (InsertQuery<UsersRecord> insertQuery = queries.createUser(ctx, user)) {
            insertQuery.execute();
            UsersRecord usersRecord = insertQuery.getReturnedRecord();
            if (usersRecord == null) {
                throw new ApplicationException(SERVER_ERROR.getCode(), "Something went wrong while creating the user.");
            }
            return usersRecord.into(User.class);
        }
    }

    /**
     * Insert access rights for a user.
     *
     * @param ctx              {@link DSLContext}
     * @param userAccessRights {@link UserAccessRights}
     * @return {@link UserAccessRights}
     */
    private UserAccessRights insertUserAccessRights(DSLContext ctx, UserAccessRights userAccessRights) throws ApplicationException {
        try (InsertQuery<UsersAccessRightsRecord> insertQuery = queries.insertUserAccessRights(ctx, userAccessRights)) {
            int insertStatus = insertQuery.execute();
            if (insertStatus != 1) {
                throw new ApplicationException(SERVER_ERROR.getCode(), "Error creating user access rights.");
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
