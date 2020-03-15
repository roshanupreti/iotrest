package com.project.iotrest.dbutil;

import com.project.iotrest.utilities.Utilities;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A simple utility class to read database properties file and create a datasource based on
 * the given database name.
 *
 * @author Roshan
 */
public class DataSourceFactory {

    private static Logger log;
    private static ResourceBundle rb;

    static {
        log = LoggerFactory.getLogger(DataSourceFactory.class);
        try {
            // Attempt to read 'database.properties'.
            rb = Utilities.getResourceBundleFromFile("database");
        } catch (MissingResourceException | MalformedURLException e) {
            log.error("Properties file not found", e);
        }
    }

    private DataSourceFactory() {
        throw new UnsupportedOperationException();
    }

    public static List<String> getDbNames() {
        Enumeration<String> keys = rb.getKeys();
        List<String> dbNames = new ArrayList<>();
        while (keys.hasMoreElements()) {
            dbNames.add(keys.nextElement());
        }
        return dbNames;
    }


    /**
     * @param dbName String
     * @return a {@link HikariDataSource} for the specified database.
     */
    public static HikariDataSource getDbDataSource(String dbName) {
        if (StringUtils.isEmpty(dbName)) {
            log.error("Cannot create datasource of an unspecified or null database!");
            System.exit(0);
        }
        log.debug("Creating data source for dbName {} .", dbName);
        return createDataSource(dbName);
    }

    /**
     * @param dbName String
     * @return a Map with connection params as key value pair.
     */
    private static Map<String, String> configMap(String dbName) {
        return Arrays.stream(rb.getString(dbName).split(",")).map(e -> e.split("="))
                .collect(Collectors
                        .toMap(keyValue -> keyValue[0].trim(),
                                keyValue -> keyValue.length > 1 ? keyValue[1].trim() : "",
                                (a, b) -> b));
    }

    /**
     * @param logicalDb String
     * @return {@link HikariDataSource} based on the database String passed as parameter.
     */
    private static HikariDataSource createDataSource(String logicalDb) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(configMap(logicalDb).get("uri") + ";INIT=CREATE SCHEMA IF NOT EXISTS IOTREST;DB_CLOSE_DELAY=-1");
        config.setUsername(configMap(logicalDb).get("uname"));
        config.setPassword(configMap(logicalDb).get("pwd"));
        return new HikariDataSource(config);
    }
}
