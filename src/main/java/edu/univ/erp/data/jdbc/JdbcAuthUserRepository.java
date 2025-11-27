package edu.univ.erp.data.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import edu.univ.erp.auth.AccountStatus;
import edu.univ.erp.auth.AuthUser;
import edu.univ.erp.data.AuthUserRepository;
import edu.univ.erp.domain.UserRole;

/**
 * JDBC implementation of {@link AuthUserRepository}.
 */
public final class JdbcAuthUserRepository implements AuthUserRepository {

    private final DataSource dataSource;

    public JdbcAuthUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<AuthUser> findByUsername(String username) {
        String sql = "SELECT user_id, username, role, password_hash, status, failed_attempts, last_login "
                + "FROM users_auth WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find user by username: " + username, ex);
        }
    }

    @Override
    public Optional<AuthUser> findById(long userId) {
        String sql = "SELECT user_id, username, role, password_hash, status, failed_attempts, last_login "
                + "FROM users_auth WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find user by ID: " + userId, ex);
        }
    }

    @Override
    public AuthUser save(AuthUser user) {
        String sql = "INSERT INTO users_auth (username, role, password_hash, status, failed_attempts) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.username());
            stmt.setString(2, user.role().name());
            stmt.setString(3, user.passwordHash());
            stmt.setString(4, user.status().name());
            stmt.setInt(5, user.failedAttempts());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    long newId = keys.getLong(1);
                    return findById(newId).orElseThrow(() -> new DataAccessException("Failed to retrieve saved user"));
                }
                throw new DataAccessException("No generated key returned");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to save user: " + user.username(), ex);
        }
    }

    @Override
    public AuthUser updateStatus(long userId, AccountStatus status) {
        String sql = "UPDATE users_auth SET status = ? WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, userId);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new DataAccessException("User not found: " + userId);
            }
            return findById(userId).orElseThrow(() -> new DataAccessException("Failed to retrieve updated user"));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user status: " + userId, ex);
        }
    }

    @Override
    public AuthUser updateRole(long userId, UserRole role) {
        String sql = "UPDATE users_auth SET role = ? WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            stmt.setLong(2, userId);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new DataAccessException("User not found: " + userId);
            }
            return findById(userId).orElseThrow(() -> new DataAccessException("Failed to retrieve updated user"));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user role: " + userId, ex);
        }
    }

    @Override
    public void updatePassword(long userId, String newHash) {
        String sql = "UPDATE users_auth SET password_hash = ?, failed_attempts = 0 WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHash);
            stmt.setLong(2, userId);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new DataAccessException("User not found: " + userId);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update password: " + userId, ex);
        }
    }

    @Override
    public void recordSuccessfulLogin(long userId) {
        String sql = "UPDATE users_auth SET last_login = ?, failed_attempts = 0 WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.from(Instant.now()));
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to record successful login: " + userId, ex);
        }
    }

    @Override
    public void recordFailedAttempt(long userId, int newCount) {
        String sql = "UPDATE users_auth SET failed_attempts = ? WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newCount);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to record failed attempt: " + userId, ex);
        }
    }

    private AuthUser mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("user_id");
        String username = rs.getString("username");
        UserRole role = UserRole.valueOf(rs.getString("role"));
        AccountStatus status = AccountStatus.valueOf(rs.getString("status"));
        String passwordHash = rs.getString("password_hash");
        int failedAttempts = rs.getInt("failed_attempts");
        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        Instant lastLogin = lastLoginTs != null ? lastLoginTs.toInstant() : null;
        return new AuthUser(id, username, role, status, passwordHash, failedAttempts, lastLogin);
    }

    @Override
    public void delete(long userId) {
        String sql = "DELETE FROM users_auth WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete user: " + userId, ex);
        }
    }

    @Override
    public List<AuthUser> findAll() {
        String sql = "SELECT user_id, username, role, password_hash, status, failed_attempts, last_login "
                + "FROM users_auth ORDER BY username";
        List<AuthUser> users = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to load auth users", ex);
        }
        return users;
    }
}

