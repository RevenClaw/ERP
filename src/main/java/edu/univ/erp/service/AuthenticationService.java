package edu.univ.erp.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import edu.univ.erp.auth.AccountStatus;
import edu.univ.erp.auth.AuthUser;
import edu.univ.erp.auth.AuthenticationException;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.AuthUserRepository;
import edu.univ.erp.data.PasswordHistoryRepository;

/**
 * Handles authentication logic and account state transitions.
 */
public class AuthenticationService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final AuthUserRepository authUserRepository;
    private final PasswordHasher passwordHasher;
    private final PasswordHistoryRepository passwordHistoryRepository;

    public AuthenticationService(AuthUserRepository authUserRepository,
                                 PasswordHasher passwordHasher,
                                 PasswordHistoryRepository passwordHistoryRepository) {
        this.authUserRepository = Objects.requireNonNull(authUserRepository, "authUserRepository");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
        this.passwordHistoryRepository = Objects.requireNonNull(passwordHistoryRepository, "passwordHistoryRepository");
    }

    public UserSession authenticate(String username, char[] password) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");

        try {
            AuthUser user = authUserRepository.findByUsername(username)
                    .orElseThrow(() -> new AuthenticationException("Incorrect username or password."));

            if (user.status() == AccountStatus.DISABLED) {
                throw new AuthenticationException("Account disabled. Contact administrator.");
            }
            if (user.status() == AccountStatus.LOCKED) {
                throw new AuthenticationException("Account locked after too many failed attempts.");
            }

            boolean matches = passwordHasher.matches(password, user.passwordHash());
            if (!matches) {
                handleFailedAttempt(user);
                throw new AuthenticationException("Incorrect username or password.");
            }

            authUserRepository.recordSuccessfulLogin(user.id());
            return new UserSession(user.id(), user.username(), user.role(), Instant.now());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public void changePassword(long userId, char[] currentPassword, char[] newPassword) {
        Objects.requireNonNull(currentPassword, "currentPassword");
        Objects.requireNonNull(newPassword, "newPassword");

        try {
            AuthUser user = authUserRepository.findById(userId)
                    .orElseThrow(() -> new AuthenticationException("User not found."));

            if (!passwordHasher.matches(currentPassword, user.passwordHash())) {
                throw new AuthenticationException("Current password is incorrect.");
            }

            String newHash = passwordHasher.hash(newPassword);

            List<String> recentHashes = passwordHistoryRepository.findRecentHashes(userId, 5);
            if (recentHashes.contains(newHash)) {
                throw new AuthenticationException("New password must differ from recently used passwords.");
            }

            passwordHistoryRepository.recordPassword(userId, newHash);
            authUserRepository.updatePassword(userId, newHash);
        } finally {
            Arrays.fill(currentPassword, '\0');
            Arrays.fill(newPassword, '\0');
        }
    }

    private void handleFailedAttempt(AuthUser user) {
        int updatedCount = user.failedAttempts() + 1;
        authUserRepository.recordFailedAttempt(user.id(), updatedCount);
        if (updatedCount >= MAX_FAILED_ATTEMPTS) {
            authUserRepository.updateStatus(user.id(), AccountStatus.LOCKED);
        }
    }
}

