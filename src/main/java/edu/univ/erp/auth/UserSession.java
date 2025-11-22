package edu.univ.erp.auth;

import java.time.Instant;
import java.util.Objects;

import edu.univ.erp.domain.UserRole;

/**
 * Represents an authenticated session for an end user.
 */
public final class UserSession {

    private final long userId;
    private final String username;
    private final UserRole role;
    private final Instant authenticatedAt;

    public UserSession(long userId, String username, UserRole role, Instant authenticatedAt) {
        this.userId = userId;
        this.username = Objects.requireNonNull(username, "username");
        this.role = Objects.requireNonNull(role, "role");
        this.authenticatedAt = Objects.requireNonNull(authenticatedAt, "authenticatedAt");
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

    public Instant authenticatedAt() {
        return authenticatedAt;
    }
}

