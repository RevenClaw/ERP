package edu.univ.erp.data;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.Instructor;

/**
 * Data access abstraction for {@link Instructor}.
 */
public interface InstructorRepository {

    Optional<Instructor> findById(long instructorId);

    Optional<Instructor> findByUserId(long userId);

    List<Instructor> findAll();

    Instructor save(Instructor instructor);

    Instructor update(Instructor instructor);
}

