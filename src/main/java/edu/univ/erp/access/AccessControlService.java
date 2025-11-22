package edu.univ.erp.access;

import java.util.EnumSet;
import java.util.Objects;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.service.MaintenanceService;

/**
 * Centralizes access checks for role permissions and maintenance mode.
 */
public class AccessControlService {

    private final SessionManager sessionManager;
    private final MaintenanceService maintenanceService;

    public AccessControlService(SessionManager sessionManager, MaintenanceService maintenanceService) {
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager");
        this.maintenanceService = Objects.requireNonNull(maintenanceService, "maintenanceService");
    }

    public UserSession ensureLoggedIn() {
        return sessionManager.getCurrentSession()
                .orElseThrow(() -> new AccessDeniedException("You must log in to continue."));
    }

    public void ensureRole(UserRole requiredRole) {
        UserSession session = ensureLoggedIn();
        if (session.role() != requiredRole) {
            throw new AccessDeniedException("Action not permitted for role: " + session.role());
        }
    }

    public void ensureRoleIn(UserRole... roles) {
        UserSession session = ensureLoggedIn();
        EnumSet<UserRole> allowed = EnumSet.noneOf(UserRole.class);
        for (UserRole role : roles) {
            allowed.add(role);
        }
        if (!allowed.contains(session.role())) {
            throw new AccessDeniedException("Action not permitted for role: " + session.role());
        }
    }

    public void ensureWritable() {
        UserSession session = ensureLoggedIn();
        if (maintenanceService.isMaintenanceMode()
                && session.role() != UserRole.ADMIN) {
            throw new AccessDeniedException("System is in maintenance mode. Changes are temporarily disabled.");
        }
    }
}

