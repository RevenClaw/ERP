package edu.univ.erp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Loads application configuration from {@code application.properties}.
 */
public final class ApplicationConfiguration {

    private static final String DEFAULT_RESOURCE = "/application.properties";

    private final DatabaseProperties authDatabase;
    private final DatabaseProperties erpDatabase;
    private final String maintenanceBanner;

    private ApplicationConfiguration(DatabaseProperties authDatabase,
                                     DatabaseProperties erpDatabase,
                                     String maintenanceBanner) {
        this.authDatabase = authDatabase;
        this.erpDatabase = erpDatabase;
        this.maintenanceBanner = maintenanceBanner;
    }

    public DatabaseProperties authDatabase() {
        return authDatabase;
    }

    public DatabaseProperties erpDatabase() {
        return erpDatabase;
    }

    public String maintenanceBanner() {
        return maintenanceBanner;
    }

    /**
     * Load configuration from the default classpath resource.
     *
     * @return application configuration
     */
    public static ApplicationConfiguration load() {
        return loadFromResource(DEFAULT_RESOURCE);
    }

    /**
     * Load configuration from a custom resource path.
     *
     * @param resourcePath classpath resource to load
     * @return application configuration
     */
    public static ApplicationConfiguration loadFromResource(String resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath");
        Properties props = new Properties();

        try (InputStream in = ApplicationConfiguration.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new ConfigurationException("Configuration resource not found: " + resourcePath);
            }
            props.load(in);
        } catch (IOException ex) {
            throw new ConfigurationException("Unable to load configuration resource: " + resourcePath, ex);
        }

        DatabaseProperties authDb = DatabaseProperties.from(props, "auth.datasource");
        DatabaseProperties erpDb = DatabaseProperties.from(props, "erp.datasource");
        String banner = props.getProperty("app.maintenance.bannerText", "Maintenance mode enabled.");

        return new ApplicationConfiguration(authDb, erpDb, banner);
    }
}

