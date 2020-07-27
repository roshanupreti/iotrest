package com.project.iotrest.application.config;

import com.project.iotrest.application.JettyServer;
import com.project.iotrest.config.JerseyAppConfig;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.DecoratingListener;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jboss.weld.environment.servlet.Listener;

import java.net.URISyntaxException;
import java.net.URL;

public class ApplicationConfig {

    private static final String CONTEXTPATH = "/";

    private ApplicationConfig() {
        throw new UnsupportedOperationException();
    }

    public static ContextHandler buildAPIContext() {
        ServletContainer servletContainer = new ServletContainer(new JerseyAppConfig());
        ServletHolder apiServletHolder = new ServletHolder(servletContainer);
        ServletContextHandler apiContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        apiContext.setContextPath(CONTEXTPATH);
        apiContext.addServlet(apiServletHolder, "/riot/*");
        apiContext.addEventListener(new DecoratingListener(apiContext));
        apiContext.addEventListener(new Listener());
        return apiContext;
    }


    public static ContextHandler buildRiotUI() throws URISyntaxException {
        URL resource = JettyServer.class.getClassLoader().getResource("META-INF/riot-ui");
        final ResourceHandler riotUIResourceHandler = new ResourceHandler();
        if (resource != null) {
            riotUIResourceHandler.setResourceBase(resource.toURI().toString());
        }
        final ContextHandler riotUIContext = new ContextHandler();
        riotUIContext.setContextPath("/riot");
        riotUIContext.setHandler(riotUIResourceHandler);
        return riotUIContext;
    }


    /* Start the Swagger UI at http://localhost:8000/iotrest/docs */
    public static ContextHandler buildSwaggerUI() throws URISyntaxException {
        URL resource = JettyServer.class.getClassLoader().getResource("META-INF/swagger-ui");
        final ResourceHandler swaggerUIResourceHandler = new ResourceHandler();
        if (resource != null) {
            swaggerUIResourceHandler.setResourceBase(resource.toURI().toString());
        }
        final ContextHandler swaggerUIContext = new ContextHandler();
        swaggerUIContext.setContextPath("/riot/docs/");
        swaggerUIContext.setHandler(swaggerUIResourceHandler);
        return swaggerUIContext;
    }

}
