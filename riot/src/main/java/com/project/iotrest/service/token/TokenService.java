package com.project.iotrest.service.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.iotrest.pojos.access.Access;
import com.project.iotrest.pojos.access.AuthenticationTokenParams;
import com.project.iotrest.pojos.user.User;
import com.project.iotrest.pojos.user.UserAccessRights;
import org.apache.commons.lang3.BooleanUtils;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides token serice.
 *
 * @author roshan
 */
@Service
public class TokenService {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final JWTProvider jwtProvider;

    @Inject
    public TokenService(JWTProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    /**
     * Generate JWT for the given user.
     *
     * @param user {@link User}
     * @return String
     */
    public JsonNode generateToken(User user) {
        ZonedDateTime iat = ZonedDateTime.now();
        Long EXPIRY_SECONDS = 900L;
        ZonedDateTime exp = iat.plusSeconds(EXPIRY_SECONDS);
        AuthenticationTokenParams authenticationTokenParams = new AuthenticationTokenParams.Builder()
                .withId(user.id().toString())
                .withUsername(user.userName())
                .withEmail(user.email())
                .withAccessRights(getAccessSet(user.userAccessRights()))
                .withIssuedDate(iat)
                .withExpirationDate(exp)
                .build();
        return mapper.valueToTree(Collections.singletonMap("access_token",
                jwtProvider.issueJWT(authenticationTokenParams)));
    }

    /**
     * Return a set of access rights from the given access rights object.
     *
     * @param userAccessRights {@link UserAccessRights}
     * @return Set
     */
    protected Set<Access> getAccessSet(UserAccessRights userAccessRights) {
        Set<Access> accessSet = new HashSet<>();

        if (BooleanUtils.isTrue(userAccessRights.create())) {
            accessSet.add(Access.CREATE);
        }
        if (BooleanUtils.isTrue(userAccessRights.read())) {
            accessSet.add(Access.READ);
        }
        if (BooleanUtils.isTrue(userAccessRights.update())) {
            accessSet.add(Access.UPDATE);
        }
        if (BooleanUtils.isTrue(userAccessRights.delete())) {
            accessSet.add(Access.DELETE);
        }
        return accessSet;
    }
}

