package com.project.iotrest.dbutil;

import com.project.iotrest.utilities.Utilities;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A simple utility class to read database properties file and create a datasource based on
 * the given database name.
 * NOW WITH DATASOURCE CACHING and EXTERNALIZED HIKARICP CONFIGURATION.
 *
 * @author Roshan
 */
@Slf4j
public class DataSourceFactory {

    private static ResourceBundle rb;
    private static ResourceBundle hikariConfig;

    // Cache DataSources to prevent creating multiple connection pools
    private static final Map<String, HikariDataSource> dataSourceCache = new ConcurrentHashMap<>();

    static {
        try {
            // Attempt to read 'database.properties'.
            rb = Utilities.getResourceBundleFromFile("database");

            // Load HikariCP configuration from external property file
            hikariConfig = Utilities.getResourceBundleFromFile("hikari");
            log.info("HikariCP configuration loaded from conf/hikari.properties");
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
     * Returns a cached {@link HikariDataSource} for the specified database.
     * Thread-safe: uses ConcurrentHashMap with computeIfAbsent.
     *
     * @param dbName String
     * @return a singleton {@link HikariDataSource} for the specified database.
     */
    public static HikariDataSource getDbDataSource(String dbName) {
        if (StringUtils.isEmpty(dbName)) {
            throw new IllegalArgumentException("Cannot create datasource of an unspecified or null database!");
        }

        return dataSourceCache.computeIfAbsent(dbName, name -> {
            log.info("Creating NEW data source for dbName: {}", name);
            return createDataSource(name);
        });
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
     * Creates and configures a new {@link HikariDataSource}.
     * Configuration is loaded from conf/hikari.properties file.
     *
     * @param logicalDb String
     * @return {@link HikariDataSource} based on the database String passed as parameter.
     */
    private static HikariDataSource createDataSource(String logicalDb) {
        HikariConfig config = new HikariConfig();

        // Basic JDBC configuration
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setJdbcUrl(configMap(logicalDb).get("uri"));
        config.setUsername(configMap(logicalDb).get("uname"));
        config.setPassword(configMap(logicalDb).get("pwd"));

        // Load HikariCP configuration from external property file
        applyHikariConfig(config, logicalDb);

        log.info("DataSource created with pool size: {} (min idle: {})",
                config.getMaximumPoolSize(),
                config.getMinimumIdle());

        return new HikariDataSource(config);
    }

    /**
     * Applies HikariCP configuration from hikari.properties file.
     * Falls back to sensible defaults if properties are missing.
     *
     * @param config HikariConfig to configure
     * @param logicalDb Database name for pool naming
     */
    private static void applyHikariConfig(HikariConfig config, String logicalDb) {
        try {
            // Connection Pool Sizing
            config.setMaximumPoolSize(
                getIntProperty("hikari.maximumPoolSize", 10));
            config.setMinimumIdle(
                getIntProperty("hikari.minimumIdle", 2));

            // Connection Timeouts
            config.setConnectionTimeout(
                getLongProperty("hikari.connectionTimeout", 30000L));
            config.setIdleTimeout(
                getLongProperty("hikari.idleTimeout", 600000L));
            config.setMaxLifetime(
                getLongProperty("hikari.maxLifetime", 1800000L));

            // Pool Behavior
            config.setAutoCommit(
                getBooleanProperty("hikari.autoCommit", true));
            config.setPoolName(
                getStringProperty("hikari.poolName", "RiotHikariPool") + "-" + logicalDb);

            // Connection Test Query (optional)
            String testQuery = getStringProperty("hikari.connectionTestQuery", null);
            if (testQuery != null && !testQuery.isEmpty()) {
                config.setConnectionTestQuery(testQuery);
            }

            // Leak Detection (0 = disabled)
            long leakThreshold = getLongProperty("hikari.leakDetectionThreshold", 0L);
            if (leakThreshold > 0) {
                config.setLeakDetectionThreshold(leakThreshold);
                log.info("Connection leak detection enabled: {}ms threshold", leakThreshold);
            }

            // Prepared Statement Cache (performance boost)
            if (getBooleanProperty("hikari.cachePrepStmts", true)) {
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize",
                    getStringProperty("hikari.prepStmtCacheSize", "250"));
                config.addDataSourceProperty("prepStmtCacheSqlLimit",
                    getStringProperty("hikari.prepStmtCacheSqlLimit", "2048"));
            }

        } catch (Exception e) {
            log.warn("Error loading HikariCP config, using defaults: {}", e.getMessage());
        }
    }

    // Helper methods to read properties with defaults

    private static String getStringProperty(String key, String defaultValue) {
        try {
            return hikariConfig != null ? hikariConfig.getString(key) : defaultValue;
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    private static int getIntProperty(String key, int defaultValue) {
        String value = getStringProperty(key, null);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.warn("Invalid integer value for {}: {}, using default: {}",
                        key, value, defaultValue);
            }
        }
        return defaultValue;
    }

    private static long getLongProperty(String key, long defaultValue) {
        String value = getStringProperty(key, null);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                log.warn("Invalid long value for {}: {}, using default: {}",
                        key, value, defaultValue);
            }
        }
        return defaultValue;
    }

    private static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getStringProperty(key, null);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Shutdown all cached DataSources (for graceful shutdown).
     * Call this on application shutdown.
     */
    public static void shutdown() {
        log.info("Shutting down all DataSources...");
        dataSourceCache.values().forEach(ds -> {
            if (!ds.isClosed()) {
                ds.close();
            }
        });
        dataSourceCache.clear();
        log.info("All DataSources closed.");
    }
}
