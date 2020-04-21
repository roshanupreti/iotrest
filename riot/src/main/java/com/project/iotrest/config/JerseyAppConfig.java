package com.project.iotrest.config;

import com.project.iotrest.loader.UserDataLoader;
import io.swagger.jaxrs.config.BeanConfig;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Jersey configuration class.
 *
 * @author roshan
 */
@ApplicationPath("/")
public class JerseyAppConfig extends ResourceConfig {
    public JerseyAppConfig() {
        register(UserDataLoader.class);
        packages(
                "jersey.config.server.provider.packages",
                "io.swagger.jaxrs.listing",
                "com.project.iotrest"
        );

        swaggerInit();
    }

    /* Swagger bean initializer. */
    private void swaggerInit() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.2");
        beanConfig.setDescription("API Documentation with Swagger");
        beanConfig.setTitle("API DOCUMENTATION");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8000");
        beanConfig.setScan(true);
        beanConfig.setBasePath("/iotrest");
        beanConfig.setResourcePackage("com.project.iotrest.rest");
    }
}