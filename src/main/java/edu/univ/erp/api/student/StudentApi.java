package edu.univ.erp.api.student;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.data.EnrollmentRepository;
import edu.univ.erp.data.SectionRepository;
import edu.univ.erp.data.StudentRepository;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.EnrollmentStatus;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Term;
import edu.univ.erp.service.StudentService;

/**
 * API facade for student operations.
 */
public class StudentApi {

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;

    public StudentApi(StudentService studentService,
                     StudentRepository studentRepository,
                     SectionRepository sectionRepository,
                     EnrollmentRepository enrollmentRepository) {
        this.studentService = Objects.requireNonNull(studentService, "studentService");
        this.studentRepository = Objects.requireNonNull(studentRepository, "studentRepository");
        this.sectionRepository = Objects.requireNonNull(sectionRepository, "sectionRepository");
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository");
    }

    public Student getStudentProfile(long userId) {
        return studentService.getStudentProfile(userId);
    }

    public List<SectionRow> getMyEnrollments(long userId, Term term, int year) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student.id());
        List<SectionRow> rows = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            if (enrollment.status() != EnrollmentStatus.ENROLLED) {
                continue;
            }

            Section section = sectionRepository.findById(enrollment.sectionId())
                    .orElseThrow(() -> new IllegalStateException("Section not found"));

            if (section.term() != term || section.year() != year) {
                continue;
            }

            // Build SectionRow (simplified - would need course/instructor details)
            rows.add(new SectionRow(
                    section.id(),
                    section.courseId(),
                    "", // Would need to fetch course
                    "",
                    section.sectionCode(),
                    "",
                    section.term(),
                    section.year(),
                    section.dayOfWeek(),
                    section.startTime(),
                    section.endTime(),
                    section.room(),
                    section.capacity(),
                    0, // Would need to count
                    0,
                    section.enrollmentDeadline(),
                    true
            ));
        }

        return rows;
    }

    public RegistrationResult registerForSection(long userId, long sectionId) {
        try {
            Student student = studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));

            studentService.registerForSection(student.id(), sectionId);
            return RegistrationResult.success("Successfully registered for section.");
        } catch (AccessDeniedException ex) {
            return RegistrationResult.failure(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return RegistrationResult.failure(ex.getMessage());
        } catch (Exception ex) {
            return RegistrationResult.failure("An error occurred: " + ex.getMessage());
        }
    }

    public DropResult dropSection(long userId, long enrollmentId) {
        try {
            Student student = studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));

            studentService.dropSection(student.id(), enrollmentId);
            return DropResult.success("Successfully dropped section.");
        } catch (AccessDeniedException ex) {
            return DropResult.failure(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return DropResult.failure(ex.getMessage());
        } catch (Exception ex) {
            return DropResult.failure("An error occurred: " + ex.getMessage());
        }
    }

    public List<Section> getTimetable(long userId, Term term, int year) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        return studentService.getStudentTimetable(student.id(), term, year);
    }

    public record RegistrationResult(boolean success, String message) {
        public static RegistrationResult success(String message) {
            return new RegistrationResult(true, message);
        }

        public static RegistrationResult failure(String message) {
            return new RegistrationResult(false, message);
        }
    }

    public record DropResult(boolean success, String message) {
        public static DropResult success(String message) {
            return new DropResult(true, message);
        }

        public static DropResult failure(String message) {
            return new DropResult(false, message);
        }
    }
}

