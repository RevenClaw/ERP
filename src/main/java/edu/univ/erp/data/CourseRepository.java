package edu.univ.erp.data;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.Course;

/**
 * Data access abstraction for {@link Course}.
 */
public interface CourseRepository {

    Optional<Course> findById(long courseId);

    Optional<Course> findByCode(String code);

    List<Course> searchByTitle(String keyword);

    List<Course> findAll();

    Course save(Course course);

    Course update(Course course);
}

