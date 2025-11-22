package edu.univ.erp.data.jdbc;

/**
 * Runtime exception for data access layer errors.
 */
public final class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

