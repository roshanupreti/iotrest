package com.project.iotrest.application.loader;

import com.project.iotrest.dbutil.DSLContextFactory;
import org.h2.tools.Server;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
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
        log.info("Attempting to prepare the internal H2 database for use.");
        try (DSLContext ctx = new DSLContextFactory().getDSLContext()) {
            debugH2();
            log.info("H2 Console available at port 8082");
            initDB(ctx);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        log.info("H2 Console available at port 8082");
        log.info("Database initialization complete!");
        return true;
    }

    /**
     * Initialize H2 web console @ localhost:8082
     */
    protected static synchronized void debugH2() throws SQLException {
        if (h2Server == null) {
            h2Server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
            h2Server.start();
            Server h2WebServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082");
            h2WebServer.start();
        }
    }

    /**
     * Create table and insert data from the respective files.
     *
     * @param ctx {@link DSLContext}
     */
    protected void initDB(DSLContext ctx) {
        ctx.query("RUNSCRIPT FROM 'classpath:/sql/schema.sql'").execute();
        ctx.query("RUNSCRIPT FROM 'classpath:/sql/data.sql'").execute();
    }

}
