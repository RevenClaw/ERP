package edu.univ.erp.data;

import java.util.Optional;

import edu.univ.erp.domain.FinalGrade;

/**
 * Data access abstraction for final grades.
 */
public interface FinalGradeRepository {

    Optional<FinalGrade> findByEnrollment(long enrollmentId);

    FinalGrade save(FinalGrade grade);

    FinalGrade update(FinalGrade grade);
}

