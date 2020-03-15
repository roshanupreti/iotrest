package com.project.iotrest.config;

import com.project.iotrest.loader.UserDataLoader;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Jersey configuration class.
 *
 * @author roshan
 */
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(UserDataLoader.class);
        packages(
                "jersey.config.server.provider.packages",
                "com.project.iotrest.config",
                "com.project.iotrest.exceptions",
                "com.project.iotrest.rest",
                "com.project.iotrest.access"
        );
    }
}