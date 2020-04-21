package com.project.iotrest.application;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.project.iotrest.application.config.ApplicationConfig.buildAPIContext;
import static com.project.iotrest.application.config.ApplicationConfig.buildSwaggerUI;

public class JettyServer {

    private static final int PORT = 8000;
    private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);

    public static void main(String[] args) {

        try {
            // Workaround for resources from JAR files
            Resource.setDefaultUseCaches(false);

            // Holds handlers
            final HandlerList handlers = new HandlerList();

            // Add handler for static swagger-UI.
            handlers.addHandler(buildSwaggerUI());

            // Add handler for APIs and Swagger.
            handlers.addHandler(buildAPIContext());

            // Start server
            Server server = new Server(PORT);
            server.setHandler(handlers);
            server.start();
            server.join();
        } catch (Exception e) {
            LOG.error("Problems encountered while starting the Server. ", e);
        }
    }
}
