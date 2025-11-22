package edu.univ.erp.data;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.Assessment;

/**
 * Data access abstraction for assessments.
 */
public interface AssessmentRepository {

    Optional<Assessment> findById(long assessmentId);

    List<Assessment> findBySection(long sectionId);

    Assessment save(Assessment assessment);

    Assessment update(Assessment assessment);

    void delete(long assessmentId);
}

