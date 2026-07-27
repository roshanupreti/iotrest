package com.project.iotrest.dbutil;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

/**
 * Static factory for {@link DSLContext}s — DAOs call {@code DSLContextFactory.getDSLContext()}
 * directly rather than injecting an instance.
 *
 * @author roshan
 */
public class DSLContextFactory {

    static final String DB_NAME = "demo";

    private static final Settings JOOQ_SETTINGS = new Settings()
            .withRenderFormatted(true)
            .withExecuteLogging(false)
            .withMapRecordComponentParameterNames(true);

    private DSLContextFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a {@link DSLContext} for the demo database.
     *
     * @return DSLContext for the demo database
     */
    public static DSLContext getDSLContext() {
        return DSL.using(DataSourceFactory.getDbDataSource(DB_NAME), SQLDialect.MARIADB, JOOQ_SETTINGS);
    }
}
