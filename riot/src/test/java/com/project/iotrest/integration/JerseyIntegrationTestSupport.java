package com.project.iotrest.integration;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * Boots a real embedded Jetty server with Jersey deployed as a genuine
 * {@link org.glassfish.jersey.servlet.ServletContainer} -- mirroring exactly how
 * {@code JettyServer#buildAPIContext} wires the production server, rather than one of Jersey's
 * lightweight standalone test containers (Grizzly, in-memory, etc.), none of which run Jersey as
 * an actual Servlet. That distinction matters here: {@link com.project.iotrest.exceptions.RESTException}
 * gets {@code @Context}-injected {@code HttpServletRequest}/{@code UriInfo} fields, which only
 * resolve inside a genuine Servlet deployment -- a non-Servlet test container would leave those
 * null and make the exception mapper itself blow up with an NPE, masking every real status code
 * behind a generic 500.
 *
 * <p>One Jetty instance is started per test class ({@code @BeforeAll}/{@code @AfterAll}) rather
 * than per test method, both for speed and to avoid rapid bind/unbind port churn.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class JerseyIntegrationTestSupport {

    private Server server;
    private WebTarget target;

    protected abstract ResourceConfig resourceConfig();

    @BeforeAll
    void startServer() throws Exception {
        server = new Server(0);
        ServletContainer servletContainer = new ServletContainer(resourceConfig());
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(new ServletHolder(servletContainer), "/*");
        server.setHandler(context);
        server.start();

        int port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        Client client = ClientBuilder.newClient();
        target = client.target("http://localhost:" + port + "/");
    }

    @AfterAll
    void stopServer() throws Exception {
        server.stop();
    }

    protected WebTarget target(String path) {
        return target.path(path);
    }
}
