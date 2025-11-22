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

import edu.univ.erp.data.AssessmentRepository;
import edu.univ.erp.domain.Assessment;

/**
 * JDBC implementation of {@link AssessmentRepository}.
 */
public final class JdbcAssessmentRepository implements AssessmentRepository {

    private final DataSource dataSource;

    public JdbcAssessmentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Assessment> findById(long assessmentId) {
        String sql = "SELECT assessment_id, section_id, name, weight_percent, max_score, created_at, updated_at "
                + "FROM assessments WHERE assessment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, assessmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find assessment by ID: " + assessmentId, ex);
        }
    }

    @Override
    public List<Assessment> findBySection(long sectionId) {
        // Use MIN(assessment_id) to get only one assessment per name, preventing duplicates
        String sql = "SELECT MIN(assessment_id) as assessment_id, section_id, name, weight_percent, max_score, MIN(created_at) as created_at, MIN(updated_at) as updated_at "
                + "FROM assessments WHERE section_id = ? "
                + "GROUP BY section_id, name, weight_percent, max_score "
                + "ORDER BY MIN(created_at)";
        List<Assessment> assessments = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assessments.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find assessments by section: " + sectionId, ex);
        }
        return assessments;
    }

    @Override
    public Assessment save(Assessment assessment) {
        String sql = "INSERT INTO assessments (section_id, name, weight_percent, max_score) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, assessment.sectionId());
            stmt.setString(2, assessment.name());
            stmt.setDouble(3, assessment.weightPercent());
            stmt.setDouble(4, assessment.maxScore());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    long newId = keys.getLong(1);
                    return findById(newId).orElseThrow(() -> new DataAccessException("Failed to retrieve saved assessment"));
                }
                throw new DataAccessException("No generated key returned");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to save assessment", ex);
        }
    }

    @Override
    public Assessment update(Assessment assessment) {
        String sql = "UPDATE assessments SET section_id = ?, name = ?, weight_percent = ?, max_score = ? "
                + "WHERE assessment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, assessment.sectionId());
            stmt.setString(2, assessment.name());
            stmt.setDouble(3, assessment.weightPercent());
            stmt.setDouble(4, assessment.maxScore());
            stmt.setLong(5, assessment.id());
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new DataAccessException("Assessment not found: " + assessment.id());
            }
            return findById(assessment.id()).orElseThrow(() -> new DataAccessException("Failed to retrieve updated assessment"));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update assessment: " + assessment.id(), ex);
        }
    }

    @Override
    public void delete(long assessmentId) {
        String sql = "DELETE FROM assessments WHERE assessment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, assessmentId);
            int deleted = stmt.executeUpdate();
            if (deleted == 0) {
                throw new DataAccessException("Assessment not found: " + assessmentId);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete assessment: " + assessmentId, ex);
        }
    }

    private Assessment mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("assessment_id");
        long sectionId = rs.getLong("section_id");
        String name = rs.getString("name");
        double weightPercent = rs.getDouble("weight_percent");
        double maxScore = rs.getDouble("max_score");
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        Instant createdAt = createdTs != null ? createdTs.toInstant() : Instant.now();
        Instant updatedAt = updatedTs != null ? updatedTs.toInstant() : Instant.now();
        return new Assessment(id, sectionId, name, weightPercent, maxScore, createdAt, updatedAt);
    }
}

