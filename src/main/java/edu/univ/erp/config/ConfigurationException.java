package edu.univ.erp.config;

/**
 * Runtime exception thrown when configuration loading fails.
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}

