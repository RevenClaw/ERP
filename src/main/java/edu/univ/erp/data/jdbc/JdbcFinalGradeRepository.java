package edu.univ.erp.data.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import javax.sql.DataSource;

import edu.univ.erp.data.FinalGradeRepository;
import edu.univ.erp.domain.FinalGrade;

/**
 * JDBC implementation of {@link FinalGradeRepository}.
 */
public final class JdbcFinalGradeRepository implements FinalGradeRepository {

    private final DataSource dataSource;

    public JdbcFinalGradeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<FinalGrade> findByEnrollment(long enrollmentId) {
        String sql = "SELECT id, enrollment_id, final_score, letter_grade, computed_at "
                + "FROM final_grades WHERE enrollment_id = ?";
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
            throw new DataAccessException("Failed to find final grade by enrollment: " + enrollmentId, ex);
        }
    }

    @Override
    public FinalGrade save(FinalGrade grade) {
        String sql = "INSERT INTO final_grades (enrollment_id, final_score, letter_grade) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE final_score = VALUES(final_score), letter_grade = VALUES(letter_grade)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, grade.enrollmentId());
            stmt.setDouble(2, grade.finalScore());
            stmt.setString(3, grade.letterGrade());
            stmt.executeUpdate();
            return findByEnrollment(grade.enrollmentId())
                    .orElseThrow(() -> new DataAccessException("Failed to retrieve saved final grade"));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to save final grade", ex);
        }
    }

    @Override
    public FinalGrade update(FinalGrade grade) {
        return save(grade);
    }

    private FinalGrade mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long enrollmentId = rs.getLong("enrollment_id");
        double finalScore = rs.getDouble("final_score");
        String letterGrade = rs.getString("letter_grade");
        Timestamp computedTs = rs.getTimestamp("computed_at");
        Instant computedAt = computedTs != null ? computedTs.toInstant() : Instant.now();
        return new FinalGrade(id, enrollmentId, finalScore, letterGrade, computedAt);
    }
}

