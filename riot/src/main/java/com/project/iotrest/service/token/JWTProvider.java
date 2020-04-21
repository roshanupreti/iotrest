package com.project.iotrest.service.token;

import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.AuthenticationTokenParams;
import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.Dependent;

import static com.project.iotrest.exceptions.ErrorStatusCodes.UNAUTHORIZED;
import static java.util.Date.from;

/**
 * This class issues and validates JWT.
 *
 * @author roshan
 */
@Dependent
public class JWTProvider {

    /* secret key to sign the JWT with. */
    private final String secret = "$2a$10$32Hw5zY7uvx0EgsfE/yQNuuL.Nrt5srepNP.vlmES2VKbYAnOBqC6";

    /**
     * Issue and return a JWT string.
     *
     * @param authenticationTokenParams {@link AuthenticationTokenParams}
     * @return String
     */
    public String issueJWT(AuthenticationTokenParams authenticationTokenParams) {
        return Jwts.builder()
                .setId(authenticationTokenParams.getId())
                .setIssuer("iotrest")
                .setSubject(authenticationTokenParams.getUsername())
                .setIssuedAt(from(authenticationTokenParams.getIssuedDate().toInstant()))
                .setExpiration(from(authenticationTokenParams.getExpirationDate().toInstant()))
                .claim("email", authenticationTokenParams.getEmail())
                .claim("access-levels", authenticationTokenParams.getAccessRights())
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * Validate the given JWT string, and return its claims.
     *
     * @param token String
     * @return {@link Claims}
     */
    public Claims parseAndValidateToken(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new RESTException(UNAUTHORIZED.getCode(), "Missing JWT.");
        }
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException e) {
            throw new RESTException(UNAUTHORIZED.getCode(), "Invalid token.");
        } catch (ExpiredJwtException e) {
            throw new RESTException(UNAUTHORIZED.getCode(), "Expired token.");
        } catch (InvalidClaimException e) {
            throw new RESTException(UNAUTHORIZED.getCode(), "Invalid claim value " + e.getClaimName());
        }
    }
}
