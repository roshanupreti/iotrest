package com.project.iotrest.application.loader;

import com.project.iotrest.dbutil.DSLContextFactory;
import org.h2.tools.Server;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class InMemoryDBLoader {

    private InMemoryDBLoader() {

    }

    private static final Logger log = LoggerFactory.getLogger(InMemoryDBLoader.class);

    private static Server h2Server = null;

    public static void initialize() throws SQLException {
        log.info("Attempting to initialize the in-memory H2 database");
        startH2();
        try (DSLContext ctx = new DSLContextFactory().getDSLContext()) {
            loadData(ctx);
        }
        log.info("H2 Console available at port 8082");
    }

    /**
     * Initialize H2 web console @ localhost:8082
     */
    protected static synchronized void startH2() throws SQLException {
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
    protected static void loadData(DSLContext ctx) {
        ctx.query("RUNSCRIPT FROM 'classpath:/sql/schema.sql'").execute();
        ctx.query("RUNSCRIPT FROM 'classpath:/sql/data.sql'").execute();
    }
}
