package edu.univ.erp.config;

import java.util.Objects;
import java.util.Properties;

/**
 * Typed view over properties for a single JDBC data source.
 */
public final class DatabaseProperties {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int maximumPoolSize;

    private DatabaseProperties(String jdbcUrl, String username, String password, int maximumPoolSize) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.maximumPoolSize = maximumPoolSize;
    }

    public String jdbcUrl() {
        return jdbcUrl;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public int maximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * Build a {@link DatabaseProperties} instance using key prefix values.
     *
     * @param props the full properties set
     * @param prefix the prefix (e.g. {@code auth.datasource})
     * @return parsed database properties
     */
    public static DatabaseProperties from(Properties props, String prefix) {
        Objects.requireNonNull(props, "props");
        Objects.requireNonNull(prefix, "prefix");

        String jdbcUrl = getRequired(props, prefix + ".jdbcUrl");
        String username = getRequired(props, prefix + ".username");
        String password = getRequired(props, prefix + ".password");
        int maxPoolSize = parseInt(props.getProperty(prefix + ".maximumPoolSize", "10"), prefix);

        return new DatabaseProperties(jdbcUrl, username, password, maxPoolSize);
    }

    private static String getRequired(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new ConfigurationException("Missing required configuration key: " + key);
        }
        return value.trim();
    }

    private static int parseInt(String value, String prefix) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new ConfigurationException("Invalid integer for " + prefix + ".maximumPoolSize: " + value, ex);
        }
    }
}

