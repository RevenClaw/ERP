package edu.univ.erp.data.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import edu.univ.erp.data.PasswordHistoryRepository;

/**
 * JDBC implementation of {@link PasswordHistoryRepository}.
 */
public final class JdbcPasswordHistoryRepository implements PasswordHistoryRepository {

    private final DataSource dataSource;

    public JdbcPasswordHistoryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void recordPassword(long userId, String passwordHash) {
        String sql = "INSERT INTO password_history (user_id, password_hash) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, passwordHash);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to record password history: " + userId, ex);
        }
    }

    @Override
    public List<String> findRecentHashes(long userId, int limit) {
        String sql = "SELECT password_hash FROM password_history "
                + "WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        List<String> hashes = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hashes.add(rs.getString("password_hash"));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find recent password hashes: " + userId, ex);
        }
        return hashes;
    }
}

