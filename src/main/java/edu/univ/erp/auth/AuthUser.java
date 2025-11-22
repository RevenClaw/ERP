package edu.univ.erp.auth;

import java.time.Instant;
import java.util.Objects;

import edu.univ.erp.domain.UserRole;

/**
 * Representation of a user in the authentication data store.
 */
public final class AuthUser {

    private final long id;
    private final String username;
    private final UserRole role;
    private final AccountStatus status;
    private final String passwordHash;
    private final int failedAttempts;
    private final Instant lastLogin;

    public AuthUser(long id,
                    String username,
                    UserRole role,
                    AccountStatus status,
                    String passwordHash,
                    int failedAttempts,
                    Instant lastLogin) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "username");
        this.role = Objects.requireNonNull(role, "role");
        this.status = Objects.requireNonNull(status, "status");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.failedAttempts = failedAttempts;
        this.lastLogin = lastLogin;
    }

    public long id() {
        return id;
    }

    public String username() {
        return username;
    }

    public UserRole role() {
        return role;
    }

    public AccountStatus status() {
        return status;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public int failedAttempts() {
        return failedAttempts;
    }

    public Instant lastLogin() {
        return lastLogin;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
}

