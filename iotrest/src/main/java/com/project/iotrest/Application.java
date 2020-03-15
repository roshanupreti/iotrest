package com.project.iotrest;

import com.project.iotrest.config.JerseyConfig;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.apache.commons.cli.*;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jboss.weld.environment.servlet.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;

import static io.undertow.servlet.Servlets.*;
import static org.glassfish.jersey.servlet.ServletProperties.JAXRS_APPLICATION_CLASS;

/* Create and start the server at `localhost:8000/` */
public class Application {

    private static final String HTTP_PORT = "httpPort";
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final int DEFAULT_HTTP_PORT = 8000;

    public static void main(final String[] args) throws ServletException, ParseException {
        System.getProperties().setProperty("org.jooq.no-logo", "true");
        final Options options = new Options();
        buildOptions(options);

        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmdLine = parser.parse(options, args);
        final int httpPort = cmdLine.hasOption(HTTP_PORT) ?
                ((Number)cmdLine.getParsedOptionValue(HTTP_PORT)).intValue() : DEFAULT_HTTP_PORT;

        final DeploymentInfo servletBuilder = getJerseyServlet();


        final DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(servletBuilder);
        deploymentManager.deploy();

        final PathHandler pathHandler = Handlers.path(Handlers.redirect("/"))
                .addPrefixPath("/", deploymentManager.start());

        LOGGER.info("Starting server, listening on port {}", httpPort);

        serverInit(httpPort, pathHandler);
    }

    /**
     * Start undertow server on the specified http port and path handler.
     *
     * @param httpPort int
     * @param pathHandler {@link PathHandler}
     */
    private static void serverInit(int httpPort, PathHandler pathHandler) {
        final Undertow server = Undertow
                .builder()
                .addHttpListener(httpPort, "0.0.0.0")
                .setHandler(pathHandler)
                .build();
        server.start();

        /* Stop server in case any RuntimeException occurs during initialization. */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Stopping server.");
            server.stop();
        }));
    }

    private static DeploymentInfo getJerseyServlet() {
        return deployment()
                .setClassLoader(Application.class.getClassLoader())
                .setResourceManager(new ClassPathResourceManager(Application.class.getClassLoader()))
                .setContextPath("/")
                .setDeploymentName("iotrest.war")
                .addServlets(
                        servlet("jerseyServlet", ServletContainer.class)
                                .setLoadOnStartup(1)
                                .addInitParam(JAXRS_APPLICATION_CLASS, JerseyConfig.class.getName())
                                .addMapping("/iotrest/*"))
                .addListener(listener(Listener.class)); // Weld listener
    }

    private static void buildOptions(Options options) {
        options.addOption(Option.builder(HTTP_PORT)
                .longOpt(HTTP_PORT)
                .required(false)
                .type(Number.class)
                .numberOfArgs(1)
                .desc("http port this server listens on")
                .build());
    }
}
