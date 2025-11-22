package edu.univ.erp.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Factory methods for HikariCP data sources backed by {@link DatabaseProperties}.
 */
public final class DataSourceFactory {

    private DataSourceFactory() {
    }

    public static DataSource create(DatabaseProperties properties, String poolName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.jdbcUrl());
        config.setUsername(properties.username());
        config.setPassword(properties.password());
        config.setMaximumPoolSize(properties.maximumPoolSize());
        config.setPoolName(poolName);
        config.setAutoCommit(true);
        config.setLeakDetectionThreshold(10_000L);
        return new HikariDataSource(config);
    }
}

