package com.project.iotrest.dao.queries;

import com.project.iotrest.pojos.user.User;
import com.project.iotrest.pojos.user.UserAccessRights;
import jooq_generated.tables.records.UsersAccessRightsRecord;
import jooq_generated.tables.records.UsersRecord;
import org.apache.commons.validator.routines.EmailValidator;
import org.jooq.*;

import javax.enterprise.context.Dependent;

import static jooq_generated.tables.Users.USERS;
import static jooq_generated.tables.UsersAccessRights.USERS_ACCESS_RIGHTS;


/**
 * Queries provider class for `users` and `users_access_rights` table.
 *
 * @author roshan
 */
@Dependent
public class UserQueries {

    /**
     * Select query for user, using id.
     *
     * @param ctx    {@link DSLContext}
     * @param userId Integer
     * @return {@link SelectConditionStep<UsersRecord>}
     */
    public SelectConditionStep<Record> getUserById(DSLContext ctx, Integer userId) {
        return ctx.select(USERS.fields())
                .select(USERS_ACCESS_RIGHTS.fields())
                .from(USERS)
                .join(USERS_ACCESS_RIGHTS).on(USERS.ID.eq(USERS_ACCESS_RIGHTS.USER_ID))
                .where(USERS.ID.eq(userId));
    }

    /**
     * Select query for user, using username or email.
     *
     * @param ctx        {@link DSLContext}
     * @param queryParam String
     * @return {@link SelectConditionStep<UsersRecord>}
     */
    public SelectConditionStep<Record> getUserByNameOrEmail(DSLContext ctx, String queryParam) {
        return ctx.select(USERS.fields())
                .select(USERS_ACCESS_RIGHTS.fields())
                .from(USERS)
                .join(USERS_ACCESS_RIGHTS).on(USERS.ID.eq(USERS_ACCESS_RIGHTS.USER_ID))
                .where(USERS.USERNAME.eq(queryParam))
                .or(USERS.EMAIL.eq(queryParam));
    }

    /**
     * Select query for user access rights, using user id.
     *
     * @param ctx    {@link DSLContext}
     * @param userId Integer
     * @return {@link SelectConditionStep<UsersAccessRightsRecord>}
     */
    public SelectConditionStep<UsersAccessRightsRecord> getUserAccessRights(DSLContext ctx, Integer userId) {
        return ctx.selectFrom(USERS_ACCESS_RIGHTS)
                .where(USERS_ACCESS_RIGHTS.USER_ID.eq(userId));
    }

    /**
     * Insert query, to create a new user.
     *
     * @param ctx  {@link DSLContext}
     * @param user {@link User}
     * @return {@link InsertQuery<UsersRecord>}
     */
    public InsertQuery<UsersRecord> createUser(DSLContext ctx, User user) {
        InsertQuery<UsersRecord> insertQuery = ctx.insertQuery(USERS);
        insertQuery.addRecord(ctx.newRecord(USERS, user));
        insertQuery.setReturning();
        return insertQuery;
    }

    /**
     * Insert query, to insert user access rights for a user.
     *
     * @param ctx          {@link DSLContext}
     * @param accessRights {@link jooq_generated.tables.UsersAccessRights}
     * @return {@link InsertQuery<UsersAccessRightsRecord>}
     */
    public InsertQuery<UsersAccessRightsRecord> insertUserAccessRights(DSLContext ctx, UserAccessRights accessRights) {
        InsertQuery<UsersAccessRightsRecord> insertQuery = ctx.insertQuery(USERS_ACCESS_RIGHTS);
        insertQuery.addRecord(ctx.newRecord(USERS_ACCESS_RIGHTS, accessRights));
        insertQuery.setReturning(USERS_ACCESS_RIGHTS.USER_ID);
        return insertQuery;
    }

    /**
     * Delete query, to delete a user.
     *
     * @param ctx    {@link DSLContext}
     * @param userId Integer
     * @return {@link DeleteConditionStep<UsersRecord>}
     */
    public DeleteConditionStep<UsersRecord> userDeleteQuery(DSLContext ctx, Integer userId) {
        return ctx.deleteFrom(USERS)
                .where(USERS.ID.eq(userId));
    }

    /**
     * Checks if the provided query parameter is username.
     *
     * @param queryParam String
     * @return Boolean
     */
    private Boolean isUsername(String queryParam) {
        return queryParam.matches("[A-Za-z0-9_]+");
    }

    /**
     * Checks if the provided query parameter is e-mail.
     *
     * @param queryParam
     * @return
     */
    private Boolean isEmail(String queryParam) {
        return EmailValidator.getInstance().isValid(queryParam);
    }
}
