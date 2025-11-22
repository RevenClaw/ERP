package edu.univ.erp.data;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.GradeEntry;

/**
 * Data access abstraction for grade entries.
 */
public interface GradeRepository {

    Optional<GradeEntry> findById(long gradeId);

    Optional<GradeEntry> findByEnrollmentAndAssessment(long enrollmentId, long assessmentId);

    List<GradeEntry> findByEnrollment(long enrollmentId);

    GradeEntry save(GradeEntry gradeEntry);

    GradeEntry update(GradeEntry gradeEntry);

    void delete(long gradeId);
}

