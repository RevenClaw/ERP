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

import edu.univ.erp.data.EnrollmentRepository;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.EnrollmentStatus;

/**
 * JDBC implementation of {@link EnrollmentRepository}.
 */
public final class JdbcEnrollmentRepository implements EnrollmentRepository {

    private final DataSource dataSource;

    public JdbcEnrollmentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Enrollment> findById(long enrollmentId) {
        String sql = "SELECT enrollment_id, student_id, section_id, status, registered_at, dropped_at "
                + "FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find enrollment by ID: " + enrollmentId, ex);
        }
    }

    @Override
    public Optional<Enrollment> findByStudentAndSection(long studentId, long sectionId) {
        String sql = "SELECT enrollment_id, student_id, section_id, status, registered_at, dropped_at "
                + "FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            stmt.setLong(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find enrollment by student and section", ex);
        }
    }

    @Override
    public List<Enrollment> findByStudent(long studentId) {
        String sql = "SELECT enrollment_id, student_id, section_id, status, registered_at, dropped_at "
                + "FROM enrollments WHERE student_id = ? ORDER BY registered_at DESC";
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find enrollments by student: " + studentId, ex);
        }
        return enrollments;
    }

    @Override
    public List<Enrollment> findBySection(long sectionId) {
        String sql = "SELECT enrollment_id, student_id, section_id, status, registered_at, dropped_at "
                + "FROM enrollments WHERE section_id = ? AND status = 'ENROLLED' ORDER BY registered_at";
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find enrollments by section: " + sectionId, ex);
        }
        return enrollments;
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        String sql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, enrollment.studentId());
            stmt.setLong(2, enrollment.sectionId());
            stmt.setString(3, enrollment.status().name());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    long newId = keys.getLong(1);
                    return findById(newId).orElseThrow(() -> new DataAccessException("Failed to retrieve saved enrollment"));
                }
                throw new DataAccessException("No generated key returned");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to save enrollment", ex);
        }
    }

    @Override
    public Enrollment updateStatus(long enrollmentId, EnrollmentStatus status) {
        String sql = "UPDATE enrollments SET status = ?, dropped_at = ? WHERE enrollment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            if (status == EnrollmentStatus.DROPPED) {
                stmt.setTimestamp(2, Timestamp.from(Instant.now()));
            } else {
                stmt.setTimestamp(2, null);
            }
            stmt.setLong(3, enrollmentId);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new DataAccessException("Enrollment not found: " + enrollmentId);
            }
            return findById(enrollmentId).orElseThrow(() -> new DataAccessException("Failed to retrieve updated enrollment"));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update enrollment status: " + enrollmentId, ex);
        }
    }

    @Override
    public int countForSection(long sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'ENROLLED'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to count enrollments for section: " + sectionId, ex);
        }
    }

    private Enrollment mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("enrollment_id");
        long studentId = rs.getLong("student_id");
        long sectionId = rs.getLong("section_id");
        EnrollmentStatus status = EnrollmentStatus.valueOf(rs.getString("status"));
        Timestamp registeredTs = rs.getTimestamp("registered_at");
        Timestamp droppedTs = rs.getTimestamp("dropped_at");
        Instant registeredAt = registeredTs != null ? registeredTs.toInstant() : Instant.now();
        Instant droppedAt = droppedTs != null ? droppedTs.toInstant() : null;
        return new Enrollment(id, studentId, sectionId, status, registeredAt, droppedAt);
    }
}

