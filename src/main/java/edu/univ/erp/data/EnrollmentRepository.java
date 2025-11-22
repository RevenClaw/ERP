package edu.univ.erp.data;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.EnrollmentStatus;

/**
 * Data access abstraction for enrollments.
 */
public interface EnrollmentRepository {

    Optional<Enrollment> findById(long enrollmentId);

    Optional<Enrollment> findByStudentAndSection(long studentId, long sectionId);

    List<Enrollment> findByStudent(long studentId);

    List<Enrollment> findBySection(long sectionId);

    Enrollment save(Enrollment enrollment);

    Enrollment updateStatus(long enrollmentId, EnrollmentStatus status);

    int countForSection(long sectionId);
}

