package edu.univ.erp.data;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.auth.AuthUser;
import edu.univ.erp.auth.AccountStatus;

/**
 * Data access abstraction for authentication users.
 */
public interface AuthUserRepository {

    Optional<AuthUser> findByUsername(String username);

    Optional<AuthUser> findById(long userId);

    AuthUser save(AuthUser user);

    AuthUser updateStatus(long userId, AccountStatus status);

    AuthUser updateRole(long userId, edu.univ.erp.domain.UserRole role);

    void updatePassword(long userId, String newHash);

    void recordSuccessfulLogin(long userId);

    void recordFailedAttempt(long userId, int newCount);

    void delete(long userId);

    List<AuthUser> findAll();
}

