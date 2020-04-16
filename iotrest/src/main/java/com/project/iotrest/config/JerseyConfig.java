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
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(UserDataLoader.class);
        packages(
                "jersey.config.server.provider.packages",
                "io.swagger.jaxrs.listing",
                "com.project.iotrest"
        );
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.2");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8000");
        beanConfig.setBasePath("/iotrest");
        beanConfig.setResourcePackage("com.project.iotrest.rest");
        beanConfig.setScan(true);
    }
}