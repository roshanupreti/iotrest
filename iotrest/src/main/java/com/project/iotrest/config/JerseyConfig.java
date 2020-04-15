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
                "io.swagger.v3.jaxrs2.integration.resources",
                "com.project.iotrest"
        );
    }
}