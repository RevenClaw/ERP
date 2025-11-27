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

import edu.univ.erp.data.StudentRepository;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.StudentStatus;

/**
 * JDBC implementation of {@link StudentRepository}.
 */
public final class JdbcStudentRepository implements StudentRepository {

    private final DataSource dataSource;

    public JdbcStudentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Student> findById(long studentId) {
        String sql = "SELECT student_id, user_id, roll_no, program, year_of_study, status, created_at, updated_at "
                + "FROM students WHERE student_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find student by ID: " + studentId, ex);
        }
    }

    @Override
    public Optional<Student> findByUserId(long userId) {
        String sql = "SELECT student_id, user_id, roll_no, program, year_of_study, status, created_at, updated_at "
                + "FROM students WHERE user_id = ?";
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
            throw new DataAccessException("Failed to find student by user ID: " + userId, ex);
        }
    }

    @Override
    public Optional<Student> findByRollNumber(String rollNumber) {
        String sql = "SELECT student_id, user_id, roll_no, program, year_of_study, status, created_at, updated_at "
                + "FROM students WHERE roll_no = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find student by roll number: " + rollNumber, ex);
        }
    }

    @Override
    public Student save(Student student) {
        String sql = "INSERT INTO students (user_id, roll_no, program, year_of_study, status) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, student.userId());
            stmt.setString(2, student.rollNumber());
            stmt.setString(3, student.program());
            stmt.setInt(4, student.yearOfStudy());
            stmt.setString(5, student.status().name());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    long newId = keys.getLong(1);
                    return findById(newId).orElseThrow(() -> new DataAccessException("Failed to retrieve saved student"));
                }
                throw new DataAccessException("No generated key returned");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to save student: " + student.rollNumber(), ex);
        }
    }

    @Override
    public Student update(Student student) {
        String sql = "UPDATE students SET roll_no = ?, program = ?, year_of_study = ?, status = ? "
                + "WHERE student_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.rollNumber());
            stmt.setString(2, student.program());
            stmt.setInt(3, student.yearOfStudy());
            stmt.setString(4, student.status().name());
            stmt.setLong(5, student.id());
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new DataAccessException("Student not found: " + student.id());
            }
            return findById(student.id()).orElseThrow(() -> new DataAccessException("Failed to retrieve updated student"));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update student: " + student.id(), ex);
        }
    }

    @Override
    public List<Student> findAll() {
        String sql = "SELECT student_id, user_id, roll_no, program, year_of_study, status, created_at, updated_at "
                + "FROM students ORDER BY roll_no";
        List<Student> students = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                students.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find all students", ex);
        }
        return students;
    }

    @Override
    public void deleteByUserId(long userId) {
        String sql = "DELETE FROM students WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete student for user: " + userId, ex);
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("student_id");
        long userId = rs.getLong("user_id");
        String rollNumber = rs.getString("roll_no");
        String program = rs.getString("program");
        int yearOfStudy = rs.getInt("year_of_study");
        StudentStatus status = StudentStatus.valueOf(rs.getString("status"));
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        Instant createdAt = createdTs != null ? createdTs.toInstant() : Instant.now();
        Instant updatedAt = updatedTs != null ? updatedTs.toInstant() : Instant.now();
        return new Student(id, userId, rollNumber, program, yearOfStudy, status, createdAt, updatedAt);
    }
}

