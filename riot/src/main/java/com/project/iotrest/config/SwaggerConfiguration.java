package com.project.iotrest.config;

import com.project.iotrest.utilities.Utilities;
import io.swagger.jaxrs.config.BeanConfig;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.net.MalformedURLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Swagger/OpenAPI documentation configuration.
 * Separated from Jersey config for better separation of concerns.
 * Auto-discovered as a JAX-RS Feature via @Provider annotation.
 *
 * <p>Configuration is loaded from conf/swagger.properties with graceful fallbacks.
 *
 * @author roshan
 */
@Slf4j
@Provider
public class SwaggerConfiguration implements Feature {

    private static ResourceBundle swaggerConfig;

    static {
        try {
            swaggerConfig = Utilities.getResourceBundleFromFile("swagger");
            log.info("Swagger configuration loaded from conf/swagger.properties");
        } catch (MissingResourceException | MalformedURLException e) {
            log.warn("Swagger configuration file not found, using defaults: {}", e.getMessage());
            swaggerConfig = null;
        }
    }

    @Override
    public boolean configure(FeatureContext context) {
        log.info("Initializing Swagger API documentation...");

        BeanConfig beanConfig = new BeanConfig();

        // API Metadata - loaded from config file or defaults
        beanConfig.setTitle(
            getProperty("swagger.title", "RIOT API Documentation"));
        beanConfig.setDescription(
            getProperty("swagger.description", "REST API for IoT Device Management with JWT Authentication"));
        beanConfig.setVersion(
            getProperty("swagger.version", "1.0.2"));

        // Server Configuration
        String schemes = getProperty("swagger.schemes", "http");
        beanConfig.setSchemes(schemes.split(","));
        beanConfig.setHost(
            getProperty("swagger.host", "localhost:8000"));
        beanConfig.setBasePath(
            getProperty("swagger.basePath", "/riot/docs"));

        // Optional Contact/License Info
        String contact = getProperty("swagger.contact", null);
        if (contact != null && !contact.isEmpty()) {
            beanConfig.setContact(contact);
        }

        String license = getProperty("swagger.license", null);
        if (license != null && !license.isEmpty()) {
            beanConfig.setLicense(license);
        }

        String licenseUrl = getProperty("swagger.licenseUrl", null);
        if (licenseUrl != null && !licenseUrl.isEmpty()) {
            beanConfig.setLicenseUrl(licenseUrl);
        }

        String termsUrl = getProperty("swagger.termsOfServiceUrl", null);
        if (termsUrl != null && !termsUrl.isEmpty()) {
            beanConfig.setTermsOfServiceUrl(termsUrl);
        }

        // Package Scanning
        beanConfig.setResourcePackage(
            getProperty("swagger.resourcePackage", "com.project.iotrest.rest"));
        beanConfig.setScan(true);

        String swaggerUrl = String.format("%s://%s%s",
            schemes.split(",")[0],
            beanConfig.getHost(),
            beanConfig.getBasePath());
        log.info("Swagger UI available at: {}", swaggerUrl);

        return true;
    }

    /**
     * Get property from config file with fallback to default value.
     *
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value or default
     */
    private static String getProperty(String key, String defaultValue) {
        if (swaggerConfig == null) {
            return defaultValue;
        }
        try {
            return swaggerConfig.getString(key);
        } catch (MissingResourceException e) {
            log.debug("Property {} not found, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }
}

