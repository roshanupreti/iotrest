package com.project.iotrest.loader;

import com.project.iotrest.dbutil.DSLContextFactory;
import org.h2.tools.Server;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * This class sets up the database with initial data, from `resources/data.sql` file during Server startup.
 *
 * @author roshan
 */
public class UserDataLoader implements Feature {

    private final Logger log = LoggerFactory.getLogger(UserDataLoader.class);

    private static Server h2Server = null;

    @Override
    public boolean configure(FeatureContext featureContext) {
        try {
            debugH2();
            log.info("H2 Console available at port 8082");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        log.info("Attempting to prepare the internal H2 database for use.");
        try (DSLContext ctx = new DSLContextFactory().getDSLContext()) {
            initDB(ctx);
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
        }
        log.info("Database initialization complete!");
        return true;
    }

    protected static synchronized void debugH2() throws SQLException {
        if (h2Server == null) {
            h2Server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
            h2Server.start();
            Server h2WebServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082");
            h2WebServer.start();
        }
    }

    private void initDB(DSLContext ctx) throws URISyntaxException {
        URL res = getClass().getClassLoader().getResource("data.sql");
        File file = null;
        if (res != null) {
            file = Paths.get(res.toURI()).toFile();
        }
        String filePath = null;
        if (file != null) {
            filePath = file.getAbsolutePath();
        }
        ctx.query("RUNSCRIPT FROM " + " '" + filePath + "'").execute();
    }

}
