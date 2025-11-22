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

import edu.univ.erp.data.CourseRepository;
import edu.univ.erp.domain.Course;

/**
 * JDBC implementation of {@link CourseRepository}.
 */
public final class JdbcCourseRepository implements CourseRepository {

    private final DataSource dataSource;

    public JdbcCourseRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Course> findById(long courseId) {
        String sql = "SELECT course_id, code, title, credits, description, created_at, updated_at "
                + "FROM courses WHERE course_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find course by ID: " + courseId, ex);
        }
    }

    @Override
    public Optional<Course> findByCode(String code) {
        String sql = "SELECT course_id, code, title, credits, description, created_at, updated_at "
                + "FROM courses WHERE code = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find course by code: " + code, ex);
        }
    }

    @Override
    public List<Course> searchByTitle(String keyword) {
        String sql = "SELECT course_id, code, title, credits, description, created_at, updated_at "
                + "FROM courses WHERE title LIKE ? OR code LIKE ? ORDER BY code";
        List<Course> courses = new ArrayList<>();
        String pattern = "%" + keyword + "%";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to search courses by title: " + keyword, ex);
        }
        return courses;
    }

    @Override
    public List<Course> findAll() {
        String sql = "SELECT course_id, code, title, credits, description, created_at, updated_at "
                + "FROM courses ORDER BY code";
        List<Course> courses = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                courses.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find all courses", ex);
        }
        return courses;
    }

    @Override
    public Course save(Course course) {
        String sql = "INSERT INTO courses (code, title, credits, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, course.code());
            stmt.setString(2, course.title());
            stmt.setDouble(3, course.credits());
            stmt.setString(4, course.description());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    long newId = keys.getLong(1);
                    return findById(newId).orElseThrow(() -> new DataAccessException("Failed to retrieve saved course"));
                }
                throw new DataAccessException("No generated key returned");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to save course: " + course.code(), ex);
        }
    }

    @Override
    public Course update(Course course) {
        String sql = "UPDATE courses SET code = ?, title = ?, credits = ?, description = ? WHERE course_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.code());
            stmt.setString(2, course.title());
            stmt.setDouble(3, course.credits());
            stmt.setString(4, course.description());
            stmt.setLong(5, course.id());
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new DataAccessException("Course not found: " + course.id());
            }
            return findById(course.id()).orElseThrow(() -> new DataAccessException("Failed to retrieve updated course"));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update course: " + course.id(), ex);
        }
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("course_id");
        String code = rs.getString("code");
        String title = rs.getString("title");
        double credits = rs.getDouble("credits");
        String description = rs.getString("description");
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        Instant createdAt = createdTs != null ? createdTs.toInstant() : Instant.now();
        Instant updatedAt = updatedTs != null ? updatedTs.toInstant() : Instant.now();
        return new Course(id, code, title, credits, description, createdAt, updatedAt);
    }
}

