package edu.univ.erp.data;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.Student;

/**
 * Data access abstraction for {@link Student} entities.
 */
public interface StudentRepository {

    Optional<Student> findById(long studentId);

    Optional<Student> findByUserId(long userId);

    Optional<Student> findByRollNumber(String rollNumber);

    Student save(Student student);

    Student update(Student student);

    List<Student> findAll();
}

