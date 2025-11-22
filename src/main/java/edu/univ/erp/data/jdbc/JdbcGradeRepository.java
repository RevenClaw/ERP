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

import edu.univ.erp.data.GradeRepository;
import edu.univ.erp.domain.GradeEntry;

/**
 * JDBC implementation of {@link GradeRepository}.
 */
public final class JdbcGradeRepository implements GradeRepository {

    private final DataSource dataSource;

    public JdbcGradeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<GradeEntry> findById(long gradeId) {
        String sql = "SELECT grade_id, enrollment_id, assessment_id, score, recorded_at "
                + "FROM grades WHERE grade_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, gradeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find grade by ID: " + gradeId, ex);
        }
    }

    @Override
    public Optional<GradeEntry> findByEnrollmentAndAssessment(long enrollmentId, long assessmentId) {
        String sql = "SELECT grade_id, enrollment_id, assessment_id, score, recorded_at "
                + "FROM grades WHERE enrollment_id = ? AND assessment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, enrollmentId);
            stmt.setLong(2, assessmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find grade by enrollment and assessment", ex);
        }
    }

    @Override
    public List<GradeEntry> findByEnrollment(long enrollmentId) {
        String sql = "SELECT grade_id, enrollment_id, assessment_id, score, recorded_at "
                + "FROM grades WHERE enrollment_id = ? ORDER BY recorded_at";
        List<GradeEntry> grades = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find grades by enrollment: " + enrollmentId, ex);
        }
        return grades;
    }

    @Override
    public GradeEntry save(GradeEntry gradeEntry) {
        String sql = "INSERT INTO grades (enrollment_id, assessment_id, score) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE score = VALUES(score)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, gradeEntry.enrollmentId());
            stmt.setLong(2, gradeEntry.assessmentId());
            stmt.setDouble(3, gradeEntry.score());
            stmt.executeUpdate();
            return findByEnrollmentAndAssessment(gradeEntry.enrollmentId(), gradeEntry.assessmentId())
                    .orElseThrow(() -> new DataAccessException("Failed to retrieve saved grade"));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to save grade", ex);
        }
    }

    @Override
    public GradeEntry update(GradeEntry gradeEntry) {
        return save(gradeEntry);
    }

    @Override
    public void delete(long gradeId) {
        String sql = "DELETE FROM grades WHERE grade_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, gradeId);
            int deleted = stmt.executeUpdate();
            if (deleted == 0) {
                throw new DataAccessException("Grade not found: " + gradeId);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete grade: " + gradeId, ex);
        }
    }

    private GradeEntry mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("grade_id");
        long enrollmentId = rs.getLong("enrollment_id");
        long assessmentId = rs.getLong("assessment_id");
        double score = rs.getDouble("score");
        Timestamp recordedTs = rs.getTimestamp("recorded_at");
        Instant recordedAt = recordedTs != null ? recordedTs.toInstant() : Instant.now();
        return new GradeEntry(id, enrollmentId, assessmentId, score, recordedAt);
    }
}

