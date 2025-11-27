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

import edu.univ.erp.data.InstructorRepository;
import edu.univ.erp.domain.Instructor;

/**
 * JDBC implementation of {@link InstructorRepository}.
 */
public final class JdbcInstructorRepository implements InstructorRepository {

    private final DataSource dataSource;

    public JdbcInstructorRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Instructor> findById(long instructorId) {
        String sql = "SELECT instructor_id, user_id, department, title, created_at, updated_at "
                + "FROM instructors WHERE instructor_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, instructorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find instructor by ID: " + instructorId, ex);
        }
    }

    @Override
    public Optional<Instructor> findByUserId(long userId) {
        String sql = "SELECT instructor_id, user_id, department, title, created_at, updated_at "
                + "FROM instructors WHERE user_id = ?";
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
            throw new DataAccessException("Failed to find instructor by user ID: " + userId, ex);
        }
    }

    @Override
    public List<Instructor> findAll() {
        String sql = "SELECT instructor_id, user_id, department, title, created_at, updated_at "
                + "FROM instructors ORDER BY department, title";
        List<Instructor> instructors = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                instructors.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find all instructors", ex);
        }
        return instructors;
    }

    @Override
    public Instructor save(Instructor instructor) {
        String sql = "INSERT INTO instructors (user_id, department, title) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, instructor.userId());
            stmt.setString(2, instructor.department());
            stmt.setString(3, instructor.title());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    long newId = keys.getLong(1);
                    return findById(newId).orElseThrow(() -> new DataAccessException("Failed to retrieve saved instructor"));
                }
                throw new DataAccessException("No generated key returned");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to save instructor: " + instructor.userId(), ex);
        }
    }

    @Override
    public Instructor update(Instructor instructor) {
        String sql = "UPDATE instructors SET department = ?, title = ? WHERE instructor_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, instructor.department());
            stmt.setString(2, instructor.title());
            stmt.setLong(3, instructor.id());
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new DataAccessException("Instructor not found: " + instructor.id());
            }
            return findById(instructor.id()).orElseThrow(() -> new DataAccessException("Failed to retrieve updated instructor"));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update instructor: " + instructor.id(), ex);
        }
    }

    private Instructor mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("instructor_id");
        long userId = rs.getLong("user_id");
        String department = rs.getString("department");
        String title = rs.getString("title");
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        Instant createdAt = createdTs != null ? createdTs.toInstant() : Instant.now();
        Instant updatedAt = updatedTs != null ? updatedTs.toInstant() : Instant.now();
        return new Instructor(id, userId, department, title, createdAt, updatedAt);
    }

    @Override
    public void deleteByUserId(long userId) {
        String sql = "DELETE FROM instructors WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete instructor for user: " + userId, ex);
        }
    }
}

