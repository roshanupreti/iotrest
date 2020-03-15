package com.project.iotrest.dbutil;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import javax.sql.DataSource;

/**
 * Provider class for {@link DSLContext} to connect to database(s).
 *
 * @author roshan
 */
public class DSLContextFactory {

    private final Configuration jooqConfig;

    /**
     * As there's just one database for now, it's stored in a final variable. Scaling can be done, for example, by passing
     * it as a constructor parameter, in case there are multiple DBs to connect to.
     */
    private static final String DB_NAME = "iotrest";

    private DataSource dataSource;

    /**
     * Constructor initiates Settings and Configuration
     *
     */
    public DSLContextFactory() {
        if (StringUtils.isEmpty(DB_NAME)) {
            throw new IllegalStateException("Cannot create a context out of a non-existent database.");
        }

        this.dataSource = DataSourceFactory.getDbDataSource(DB_NAME);

        jooqConfig = new DefaultConfiguration();

        Settings jooqSettings = new Settings()
                .withRenderMapping(new RenderMapping().withSchemata(
                        new MappedSchema().withInput(DB_NAME).withOutput(DB_NAME)))
                .withRenderFormatted(true)
                .withExecuteLogging(false);

        jooqConfig.set(jooqSettings);
        jooqConfig.set(SQLDialect.H2);
    }

    /**
     * @return DSLContext
     */
    public DSLContext getDSLContext() {
        return DSL.using(jooqConfig.derive(dataSource));
    }
}
