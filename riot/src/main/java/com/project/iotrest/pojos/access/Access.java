package com.project.iotrest.pojos.access;

import com.project.iotrest.exceptions.RESTException;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enum class representing users' possible access rights.
 *
 * @author roshan
 */
public enum Access {

    CREATE("create"),
    READ("read"),
    UPDATE("update"),
    DELETE("delete");

    private final String level;

    Access(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

    public static Access fromString(String text) {
        Optional<Access> optionalAccess = Arrays.stream(Access.values())
                .filter(e -> e.getLevel().equalsIgnoreCase(text))
                .findAny();
        if (optionalAccess.isPresent()) {
            return optionalAccess.get();
        } else {
            throw new RESTException(400, "Invalid access level.");
        }
    }
}

