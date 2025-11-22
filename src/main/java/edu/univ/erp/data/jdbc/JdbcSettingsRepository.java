package edu.univ.erp.data.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import javax.sql.DataSource;

import edu.univ.erp.data.SettingsRepository;
import edu.univ.erp.domain.AppSetting;

/**
 * JDBC implementation of {@link SettingsRepository}.
 */
public final class JdbcSettingsRepository implements SettingsRepository {

    private final DataSource dataSource;

    public JdbcSettingsRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<AppSetting> findByKey(String key) {
        String sql = "SELECT setting_key, setting_value, updated_at FROM settings WHERE setting_key = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find setting: " + key, ex);
        }
    }

    @Override
    public AppSetting save(AppSetting setting) {
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, setting.key());
            stmt.setString(2, setting.value());
            stmt.executeUpdate();
            return findByKey(setting.key()).orElseThrow(() -> new DataAccessException("Failed to retrieve saved setting"));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to save setting: " + setting.key(), ex);
        }
    }

    @Override
    public AppSetting update(AppSetting setting) {
        return save(setting);
    }

    private AppSetting mapRow(ResultSet rs) throws SQLException {
        String key = rs.getString("setting_key");
        String value = rs.getString("setting_value");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        Instant updatedAt = updatedTs != null ? updatedTs.toInstant() : Instant.now();
        return new AppSetting(key, value, updatedAt);
    }
}

