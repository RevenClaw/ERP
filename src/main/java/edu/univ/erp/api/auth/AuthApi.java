package edu.univ.erp.api.auth;

import java.util.Objects;

import edu.univ.erp.api.types.LoginRequest;
import edu.univ.erp.api.types.LoginResponse;
import edu.univ.erp.auth.AuthenticationException;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.service.AuthenticationService;
import edu.univ.erp.service.MaintenanceService;

/**
 * Facade exposing authentication use-cases to the UI layer.
 */
public class AuthApi {

    private final AuthenticationService authenticationService;
    private final MaintenanceService maintenanceService;
    private final SessionManager sessionManager;

    public AuthApi(AuthenticationService authenticationService,
                   MaintenanceService maintenanceService,
                   SessionManager sessionManager) {
        this.authenticationService = Objects.requireNonNull(authenticationService, "authenticationService");
        this.maintenanceService = Objects.requireNonNull(maintenanceService, "maintenanceService");
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager");
    }

    public LoginResponse login(LoginRequest request) {
        Objects.requireNonNull(request, "request");
        try {
            UserSession session = authenticationService.authenticate(request.username(), request.password());
            sessionManager.login(session);
            boolean maintenanceMode = maintenanceService.isMaintenanceMode();
            return LoginResponse.success(session.userId(), session.username(), session.role(), maintenanceMode);
        } catch (AuthenticationException ex) {
            return LoginResponse.failure(ex.getMessage());
        } finally {
            request.clearPassword();
        }
    }

    public void logout() {
        sessionManager.logout();
    }
}

