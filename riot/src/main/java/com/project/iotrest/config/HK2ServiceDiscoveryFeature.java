package com.project.iotrest.config;

import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.Populator;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ClasspathDescriptorFileFinder;
import org.glassfish.hk2.utilities.DuplicatePostProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Loads the {@code @Service}-annotated class descriptors that {@code hk2-metadata-generator}
 * compiles into {@code META-INF/hk2-locator/default} into Jersey's HK2 {@link ServiceLocator}.
 *
 * <p>Without this, {@code @Service} classes (e.g. {@code JWTProvider}, {@code UserDao}) are never
 * actually registered with Jersey's injector — the generated descriptor file just sits unused on
 * the classpath and every {@code @Inject} of one of these classes fails at request time with
 * {@code UnsatisfiedDependencyException}.
 *
 * @author roshan
 */
@Slf4j
@Provider
public class HK2ServiceDiscoveryFeature implements Feature {

    private final ServiceLocator locator;

    @Inject
    public HK2ServiceDiscoveryFeature(ServiceLocator locator) {
        this.locator = locator;
    }

    @Override
    public boolean configure(FeatureContext context) {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Populator populator = dcs.getPopulator();
        try {
            populator.populate(
                    new ClasspathDescriptorFileFinder(HK2ServiceDiscoveryFeature.class.getClassLoader()),
                    new DuplicatePostProcessor());
        } catch (IOException | MultiException e) {
            log.error("Failed to populate HK2 ServiceLocator from generated @Service descriptors", e);
            return false;
        }
        return true;
    }
}
