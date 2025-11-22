package edu.univ.erp.data.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import edu.univ.erp.data.SectionRepository;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Term;
import edu.univ.erp.domain.Weekday;

/**
 * JDBC implementation of {@link SectionRepository}.
 */
public final class JdbcSectionRepository implements SectionRepository {

    private final DataSource dataSource;

    public JdbcSectionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Section> findById(long sectionId) {
        String sql = "SELECT section_id, course_id, instructor_id, semester, year, section_code, "
                + "day_of_week, start_time, end_time, room, capacity, enrollment_deadline, created_at, updated_at "
                + "FROM sections WHERE section_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find section by ID: " + sectionId, ex);
        }
    }

    @Override
    public List<Section> findByCourse(long courseId, Term term, int year) {
        String sql = "SELECT section_id, course_id, instructor_id, semester, year, section_code, "
                + "day_of_week, start_time, end_time, room, capacity, enrollment_deadline, created_at, updated_at "
                + "FROM sections WHERE course_id = ? AND semester = ? AND year = ? ORDER BY section_code";
        return findSections(sql, courseId, term, year);
    }

    @Override
    public List<Section> findByInstructor(long instructorId, Term term, int year) {
        String sql = "SELECT section_id, course_id, instructor_id, semester, year, section_code, "
                + "day_of_week, start_time, end_time, room, capacity, enrollment_deadline, created_at, updated_at "
                + "FROM sections WHERE instructor_id = ? AND semester = ? AND year = ? ORDER BY section_code";
        return findSections(sql, instructorId, term, year);
    }

    @Override
    public List<Section> findStudentSections(long studentId, Term term, int year) {
        String sql = "SELECT s.section_id, s.course_id, s.instructor_id, s.semester, s.year, s.section_code, "
                + "s.day_of_week, s.start_time, s.end_time, s.room, s.capacity, s.enrollment_deadline, "
                + "s.created_at, s.updated_at "
                + "FROM sections s "
                + "INNER JOIN enrollments e ON s.section_id = e.section_id "
                + "WHERE e.student_id = ? AND e.status = 'ENROLLED' AND s.semester = ? AND s.year = ? "
                + "ORDER BY s.day_of_week, s.start_time";
        return findSections(sql, studentId, term, year);
    }

    @Override
    public List<Section> findByTerm(Term term, int year) {
        String sql = "SELECT section_id, course_id, instructor_id, semester, year, section_code, "
                + "day_of_week, start_time, end_time, room, capacity, enrollment_deadline, created_at, updated_at "
                + "FROM sections WHERE semester = ? AND year = ? ORDER BY course_id, section_code";
        List<Section> sections = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, term.name());
            stmt.setInt(2, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sections.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find sections by term", ex);
        }
        return sections;
    }

    @Override
    public Section save(Section section) {
        String sql = "INSERT INTO sections (course_id, instructor_id, semester, year, section_code, "
                + "day_of_week, start_time, end_time, room, capacity, enrollment_deadline) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, section.courseId());
            stmt.setLong(2, section.instructorId());
            stmt.setString(3, section.term().name());
            stmt.setInt(4, section.year());
            stmt.setString(5, section.sectionCode());
            stmt.setString(6, section.dayOfWeek().name());
            stmt.setTime(7, Time.valueOf(section.startTime()));
            stmt.setTime(8, Time.valueOf(section.endTime()));
            stmt.setString(9, section.room());
            stmt.setInt(10, section.capacity());
            stmt.setDate(11, Date.valueOf(section.enrollmentDeadline()));
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    long newId = keys.getLong(1);
                    return findById(newId).orElseThrow(() -> new DataAccessException("Failed to retrieve saved section"));
                }
                throw new DataAccessException("No generated key returned");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to save section", ex);
        }
    }

    @Override
    public Section update(Section section) {
        String sql = "UPDATE sections SET course_id = ?, instructor_id = ?, semester = ?, year = ?, "
                + "section_code = ?, day_of_week = ?, start_time = ?, end_time = ?, room = ?, "
                + "capacity = ?, enrollment_deadline = ? WHERE section_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, section.courseId());
            stmt.setLong(2, section.instructorId());
            stmt.setString(3, section.term().name());
            stmt.setInt(4, section.year());
            stmt.setString(5, section.sectionCode());
            stmt.setString(6, section.dayOfWeek().name());
            stmt.setTime(7, Time.valueOf(section.startTime()));
            stmt.setTime(8, Time.valueOf(section.endTime()));
            stmt.setString(9, section.room());
            stmt.setInt(10, section.capacity());
            stmt.setDate(11, Date.valueOf(section.enrollmentDeadline()));
            stmt.setLong(12, section.id());
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new DataAccessException("Section not found: " + section.id());
            }
            return findById(section.id()).orElseThrow(() -> new DataAccessException("Failed to retrieve updated section"));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update section: " + section.id(), ex);
        }
    }

    @Override
    public List<Section> findAll() {
        String sql = "SELECT section_id, course_id, instructor_id, semester, year, section_code, "
                + "day_of_week, start_time, end_time, room, capacity, enrollment_deadline, created_at, updated_at "
                + "FROM sections ORDER BY year DESC, semester, course_id, section_code";
        List<Section> sections = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sections.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find all sections", ex);
        }
        return sections;
    }

    @Override
    public void delete(long sectionId) {
        String sql = "DELETE FROM sections WHERE section_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sectionId);
            int deleted = stmt.executeUpdate();
            if (deleted == 0) {
                throw new DataAccessException("Section not found: " + sectionId);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete section: " + sectionId, ex);
        }
    }

    private List<Section> findSections(String sql, long param1, Term term, int year) {
        List<Section> sections = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, param1);
            stmt.setString(2, term.name());
            stmt.setInt(3, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sections.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find sections", ex);
        }
        return sections;
    }

    private Section mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("section_id");
        long courseId = rs.getLong("course_id");
        long instructorId = rs.getLong("instructor_id");
        Term term = Term.valueOf(rs.getString("semester"));
        int year = rs.getInt("year");
        String sectionCode = rs.getString("section_code");
        Weekday dayOfWeek = Weekday.valueOf(rs.getString("day_of_week"));
        Time startTime = rs.getTime("start_time");
        Time endTime = rs.getTime("end_time");
        String room = rs.getString("room");
        int capacity = rs.getInt("capacity");
        Date deadline = rs.getDate("enrollment_deadline");
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        Instant createdAt = createdTs != null ? createdTs.toInstant() : Instant.now();
        Instant updatedAt = updatedTs != null ? updatedTs.toInstant() : Instant.now();
        return new Section(id, courseId, instructorId, term, year, sectionCode, dayOfWeek,
                startTime.toLocalTime(), endTime.toLocalTime(), room, capacity,
                deadline.toLocalDate(), createdAt, updatedAt);
    }
}

