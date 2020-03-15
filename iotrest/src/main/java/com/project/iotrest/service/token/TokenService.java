package com.project.iotrest.service.token;

import com.project.iotrest.pojos.Access;
import com.project.iotrest.pojos.AuthenticationTokenParams;
import com.project.iotrest.pojos.User;
import com.project.iotrest.pojos.UserAccessRights;
import org.apache.commons.lang3.BooleanUtils;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides token serice.
 *
 * @author roshan
 */
@Dependent
public class TokenService {

    private final Long EXPIRY_SECONDS = 900L;

    @Inject
    private JWTProvider jwtProvider;

    /**
     * Generate JWT for the given user.
     *
     * @param user {@link User}
     * @return String
     */
    public String generateToken(User user) {
        ZonedDateTime iat = ZonedDateTime.now();
        ZonedDateTime exp = iat.plusSeconds(EXPIRY_SECONDS);
        AuthenticationTokenParams authenticationTokenParams = new AuthenticationTokenParams.Builder()
                .withId(user.getId().toString())
                .withUsername(user.getUserName())
                .withEmail(user.getEmail())
                .withAccessRights(getAccessSet(user.getUserAccessRights()))
                .withIssuedDate(iat)
                .withExpirationDate(exp)
                .build();
        return jwtProvider.issueJWT(authenticationTokenParams);
    }

    /**
     * Return a set of access rights from the given access rights object.
     *
     * @param userAccessRights {@link UserAccessRights}
     * @return Set
     */
    protected Set<Access> getAccessSet(UserAccessRights userAccessRights) {
        Set<Access> accessSet = new HashSet<>();

        if (BooleanUtils.isTrue(userAccessRights.getCreate())) {
            accessSet.add(Access.CREATE);
        }
        if (BooleanUtils.isTrue(userAccessRights.getRead())) {
            accessSet.add(Access.READ);
        }
        if (BooleanUtils.isTrue(userAccessRights.getUpdate())) {
            accessSet.add(Access.UPDATE);
        }
        if (BooleanUtils.isTrue(userAccessRights.getDelete())) {
            accessSet.add(Access.DELETE);
        }
        return accessSet;
    }
}

