package com.project.iotrest.validation;

import com.project.iotrest.exceptions.RESTException;
import com.project.iotrest.pojos.user.User;
import org.apache.commons.lang3.StringUtils;

/**
 * Request parameters validator class.
 *
 * @author roshan
 */
public class RequestParamValidator {

    private RequestParamValidator() {
        throw new UnsupportedOperationException();
    }

    public static void validateId(Integer id) {
        if (id == null
                || id == 0) {
            throw new RESTException(400, "Null or zero ID");
        }
    }

    public static void validateString(String stringParam) {
        if (StringUtils.isEmpty(stringParam)) {
            throw new RESTException(400, "Null or empty " + stringParam);
        }
    }

    public static void validatePost(User user) {
        StringBuilder message = new StringBuilder();
        if (StringUtils.isEmpty(user.getUserName())) {
            message.append("Empty username");
        }
        if (StringUtils.isEmpty(user.getEmail())) {
            message.append("Empty email");
        }
        if (!StringUtils.isEmpty(message)) {
            throw new RESTException(400, message.toString());
        }
    }

}
