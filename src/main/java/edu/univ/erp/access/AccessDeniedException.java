package edu.univ.erp.access;

/**
 * Thrown when an action is not permitted for the current session.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}

