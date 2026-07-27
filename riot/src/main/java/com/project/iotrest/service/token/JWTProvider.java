package com.project.iotrest.service.token;

import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.access.AuthenticationTokenParams;
import com.project.iotrest.utilities.Utilities;
import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;
import org.jvnet.hk2.annotations.Service;

import java.net.MalformedURLException;

import static com.project.iotrest.exceptions.ErrorStatusCodes.UNAUTHORIZED;
import static java.util.Date.from;

/**
 * This class issues and validates JWT.
 *
 * @author roshan
 */
@Service
public class JWTProvider {

    private static final String JWT_CONFIG_FILE = "jwt/jwt";
    private static final String JWT_SECRET_KEY = "secret";

    /* secret key to sign the JWT with, loaded from a gitignored config file (conf/jwt/jwt.properties). */
    private final String secret;

    public JWTProvider() throws MalformedURLException {
        this.secret = Utilities.getResourceBundleFromFile(JWT_CONFIG_FILE).getString(JWT_SECRET_KEY);
    }

    /**
     * Issue and return a JWT string.
     *
     * @param authenticationTokenParams {@link AuthenticationTokenParams}
     * @return String
     */
    public String issueJWT(AuthenticationTokenParams authenticationTokenParams) {
        return Jwts.builder()
                .setId(authenticationTokenParams.id())
                .setIssuer("iotrest")
                .setSubject(authenticationTokenParams.username())
                .setIssuedAt(from(authenticationTokenParams.issuedDate().toInstant()))
                .setExpiration(from(authenticationTokenParams.expirationDate().toInstant()))
                .claim("id", Integer.valueOf(authenticationTokenParams.id()))
                .claim("email", authenticationTokenParams.email())
                .claim("access-levels", authenticationTokenParams.accessRights())
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
