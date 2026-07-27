package com.project.iotrest.config;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Jersey/JAX-RS application configuration.
 * Configures HK2 auto-discovery for dependency injection.
 * 
 * <p>Swagger configuration is handled separately by {@link SwaggerConfiguration}
 * which is auto-discovered as a JAX-RS Feature.
 *
 * @author roshan
 */
@ApplicationPath("/")
public class JerseyAppConfig extends ResourceConfig {
    
    public JerseyAppConfig() {
        // Enable package scanning for:
        // - JAX-RS resources (@Path)
        // - Providers (@Provider) - filters, exception mappers, features
        // - HK2 services (@Service) - for dependency injection
        // - Swagger API listing endpoints
        packages(
                "jersey.config.server.provider.packages",
                "io.swagger.jaxrs.listing",
                "com.project.iotrest"
        );

        // Note: Services are auto-discovered via @Service annotation (HK2)
        // Note: Swagger is auto-configured via SwaggerConfiguration Feature
    }
}

