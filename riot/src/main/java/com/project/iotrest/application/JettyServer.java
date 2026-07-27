package com.project.iotrest.application;

import com.project.iotrest.config.JerseyAppConfig;
import com.project.iotrest.utilities.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class JettyServer {

    private static final int HTTPS_PORT = 8443;
    private static final String JETTY_CONFIG_FILE = "jetty/jetty";

    public static void main(String[] args) {

        try {
            // Workaround for resources from JAR files
            Resource.setDefaultUseCaches(false);

            // Holds handlers
            final HandlerList handlers = new HandlerList();

            // Add handler for static swagger-UI.
            handlers.addHandler(buildStaticUIContext("META-INF/swagger-ui", "/riot/docs/"));

            handlers.addHandler(buildStaticUIContext("META-INF/riot-ui", "/riot"));

            // Add handler for APIs and Swagger.
            handlers.addHandler(buildAPIContext());

            // Start server (HTTPS-only, no plaintext HTTP connector)
            Server server = new Server();
            server.setHandler(handlers);
            server.addConnector(buildHttpsConnector(server));
            server.start();
            server.join();
        } catch (InterruptedException e) {
            log.error("Server was interrupted.", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Problems encountered while starting the Server. ", e);
        }
    }

    /**
     * Builds the (sole) HTTPS connector. Keystore path/password are read from a gitignored
     * config file (conf/jetty/jetty.properties) rather than hardcoded, mirroring how JWTProvider
     * reads its signing secret.
     */
    private static ServerConnector buildHttpsConnector(Server server) throws Exception {
        ResourceBundle jettyConfig = Utilities.getResourceBundleFromFile(JETTY_CONFIG_FILE);

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(jettyConfig.getString("keystore.path"));
        sslContextFactory.setKeyStorePassword(jettyConfig.getString("keystore.password"));

        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecureScheme("https");
        httpConfiguration.setSecurePort(HTTPS_PORT);
        httpConfiguration.addCustomizer(new SecureRequestCustomizer());

        ServerConnector httpsConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(httpConfiguration));
        httpsConnector.setPort(HTTPS_PORT);
        return httpsConnector;
    }

    private static ContextHandler buildAPIContext() {
        ServletContainer servletContainer = new ServletContainer(new JerseyAppConfig());
        ServletHolder apiServletHolder = new ServletHolder(servletContainer);
        ServletContextHandler apiContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        apiContext.setContextPath("/");
        apiContext.addServlet(apiServletHolder, "/riot/*");
        return apiContext;
    }

    /**
     * Builds a static-resource handler serving a classpath directory (e.g. the login UI or
     * Swagger UI) under the given context path.
     */
    private static ContextHandler buildStaticUIContext(String classpathResource, String contextPath)
            throws URISyntaxException {
        URL resource = JettyServer.class.getClassLoader().getResource(classpathResource);
        final ResourceHandler resourceHandler = new ResourceHandler();
        if (resource != null) {
            resourceHandler.setResourceBase(resource.toURI().toString());
        }
        final ContextHandler context = new ContextHandler();
        context.setContextPath(contextPath);
        context.setHandler(resourceHandler);
        return context;
    }
}
