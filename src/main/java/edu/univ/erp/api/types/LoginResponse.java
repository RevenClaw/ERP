package edu.univ.erp.api.types;

import edu.univ.erp.domain.UserRole;

/**
 * DTO returned to the UI after login attempt.
 */
public final class LoginResponse {

    private final boolean success;
    private final String message;
    private final long userId;
    private final String username;
    private final UserRole role;
    private final boolean maintenanceMode;

    private LoginResponse(boolean success,
                          String message,
                          long userId,
                          String username,
                          UserRole role,
                          boolean maintenanceMode) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.maintenanceMode = maintenanceMode;
    }

    public static LoginResponse success(long userId, String username, UserRole role, boolean maintenanceMode) {
        return new LoginResponse(true, "Login successful.", userId, username, role, maintenanceMode);
    }

    public static LoginResponse failure(String message) {
        return new LoginResponse(false, message, -1L, null, null, false);
    }

    public boolean success() {
        return success;
    }

    public String message() {
        return message;
    }

    public long userId() {
        return userId;
    }

    public String username() {
        return username;
    }

    public UserRole role() {
        return role;
    }

    public boolean maintenanceMode() {
        return maintenanceMode;
    }
}

