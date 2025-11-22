package edu.univ.erp.auth;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple in-memory session holder for the desktop client.
 */
public class SessionManager {

    private final AtomicReference<UserSession> currentSession = new AtomicReference<>();

    public Optional<UserSession> getCurrentSession() {
        return Optional.ofNullable(currentSession.get());
    }

    public void login(UserSession session) {
        currentSession.set(session);
    }

    public void logout() {
        currentSession.set(null);
    }
}

