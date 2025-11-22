package edu.univ.erp.data;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Term;

/**
 * Data access abstraction for course sections.
 */
public interface SectionRepository {

    Optional<Section> findById(long sectionId);

    List<Section> findByCourse(long courseId, Term term, int year);

    List<Section> findByInstructor(long instructorId, Term term, int year);

    List<Section> findStudentSections(long studentId, Term term, int year);

    List<Section> findByTerm(Term term, int year);

    List<Section> findAll();

    Section save(Section section);

    Section update(Section section);

    void delete(long sectionId);
}

