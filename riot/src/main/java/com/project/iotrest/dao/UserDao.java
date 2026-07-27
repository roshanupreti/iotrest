package com.project.iotrest.dao;

import com.project.iotrest.dbutil.DSLContextFactory;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.ErrorStatusCodes;
import com.project.iotrest.pojos.user.User;
import com.project.iotrest.pojos.user.UserAccessRights;
import jooq_generated.tables.records.UsersAccessRightsRecord;
import jooq_generated.tables.records.UsersRecord;
import org.apache.commons.collections.MapUtils;
import org.jooq.DSLContext;
import org.jooq.InsertQuery;
import org.jvnet.hk2.annotations.Service;

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
@Service
public class UserDao {

    private final UserQueries queries;

    @Inject
    public UserDao(UserQueries queries) {
        this.queries = queries;
    }

    /**
     * Create new User
     *
     * @param user {@link User}
     * @return {@link User}
     */
    public User createUser(User user) throws ApplicationException {
        DSLContext ctx = DSLContextFactory.getDSLContext();
        User createdUser = performUserInsert(ctx, user);
        if (user.userAccessRights() != null) {
            // Insert Access Rights.
            UserAccessRights userAccessRights = insertUserAccessRights(ctx, new UserAccessRights(createdUser.id(),
                    user.userAccessRights().create(),
                    user.userAccessRights().read(),
                    user.userAccessRights().update(),
                    user.userAccessRights().delete()));
            createdUser = withUserAccessRightsIfPresent(createdUser, userAccessRights);
        }
        return createdUser;
    }

    /**
     * Find a user by id.
     *
     * @param id Integer
     * @return {@link User}
     */
    public User getUserById(Integer id) throws ApplicationException {
        DSLContext ctx = DSLContextFactory.getDSLContext();
        Map<User, List<UserAccessRights>> result = queries.getUserById(ctx, id)
                .fetchGroups(
                        // Map records first into the USERS table and then into the key POJO type
                        r -> r.into(USERS).into(User.class),
                        // Map records first into the USERS_ACCESS_RIGHTS table and then into the value POJO type
                        r -> r.into(USERS_ACCESS_RIGHTS).into(UserAccessRights.class)
                );
        User user = getUserAndAccessRights(User.empty(), result);
        if (user.id() == null) {
            throw new ApplicationException(ErrorStatusCodes.NOT_FOUND.getCode(), "User not found with id " + id);
        }
        return user;
    }

    /**
     * Find a user by username or email.
     *
     * @param queryParam String
     * @return {@link User}
     */
    public User getUserByUserNameOrEmail(String queryParam) throws ApplicationException {
        DSLContext ctx = DSLContextFactory.getDSLContext();
        Map<User, List<UserAccessRights>> result = queries.getUserByNameOrEmail(ctx, queryParam)
                .fetchGroups(
                        // Map records first into the USERS table and then into the key POJO type
                        r -> r.into(USERS).into(User.class),
                        // Map records first into the USERS_ACCESS_RIGHTS table and then into the value POJO type
                        r -> r.into(USERS_ACCESS_RIGHTS).into(UserAccessRights.class)
                );
        User user = getUserAndAccessRights(User.empty(), result);
        if (user.id() == null) {
            throw new ApplicationException(ErrorStatusCodes.NOT_FOUND.getCode(),
                    "User not found with username and/or " +
                    "email " + queryParam);
        }
        return user;
    }

    private User getUserAndAccessRights(User user, Map<User, List<UserAccessRights>> result) {
        if (MapUtils.isNotEmpty(result)) {
            for (Map.Entry<User, List<UserAccessRights>> entry : result.entrySet()) {
                if (entry.getValue().size() <= 1) {
                    UserAccessRights userAccessRights = entry.getValue().get(0);
                    user = entry.getKey().withUserAccessRights(userAccessRights);
                }
            }
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
        InsertQuery<UsersRecord> insertQuery = queries.createUser(ctx, user);
        insertQuery.execute();
        UsersRecord usersRecord = insertQuery.getReturnedRecord();
        if (usersRecord == null) {
            throw new ApplicationException(SERVER_ERROR.getCode(), "Something went wrong while creating the user.");
        }
        return usersRecord.into(User.class);
    }

    /**
     * Insert access rights for a user.
     *
     * @param ctx              {@link DSLContext}
     * @param userAccessRights {@link UserAccessRights}
     * @return {@link UserAccessRights}
     */
    private UserAccessRights insertUserAccessRights(DSLContext ctx, UserAccessRights userAccessRights) throws ApplicationException {
        InsertQuery<UsersAccessRightsRecord> insertQuery = queries.insertUserAccessRights(ctx, userAccessRights);
        int insertStatus = insertQuery.execute();
        if (insertStatus != 1) {
            throw new ApplicationException(SERVER_ERROR.getCode(), "Error creating user access rights.");
        }
        return ctx.selectFrom(USERS_ACCESS_RIGHTS)
                .where(USERS_ACCESS_RIGHTS.USER_ID.eq(userAccessRights.userId()))
                .fetchOne()
                .into(UserAccessRights.class);
    }

    /**
     * Delete a user, based on the provided id.
     *
     * @param userId Integer
     * @return int
     */
    public int deleteUser(Integer userId) {
        DSLContext ctx = DSLContextFactory.getDSLContext();
        return queries.userDeleteQuery(ctx, userId).execute();
    }

    /**
     * Checks if the user with the provided identifier exists.
     *
     * @param usernameOrEmail String
     * @return Boolean
     */
    public Boolean userExists(String usernameOrEmail) {
        DSLContext ctx = DSLContextFactory.getDSLContext();
        return ctx.fetchExists(
                queries.getUserByNameOrEmail(ctx, usernameOrEmail)
        );
    }

    /**
     * Returns a copy of the provided user with the given access rights attached, if present.
     *
     * @param user User to update
     * @param userAccessRights Access rights to attach
     * @return User with access rights attached
     */
    private User withUserAccessRightsIfPresent(User user, UserAccessRights userAccessRights) {
        return userAccessRights != null ? user.withUserAccessRights(userAccessRights) : user;
    }
}
