package edu.univ.erp.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import edu.univ.erp.access.AccessControlService;
import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.data.CourseRepository;
import edu.univ.erp.data.EnrollmentRepository;
import edu.univ.erp.data.SectionRepository;
import edu.univ.erp.data.StudentRepository;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.EnrollmentStatus;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Term;

/**
 * Business logic for student operations.
 */
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AccessControlService accessControl;

    public StudentService(StudentRepository studentRepository,
                         CourseRepository courseRepository,
                         SectionRepository sectionRepository,
                         EnrollmentRepository enrollmentRepository,
                         AccessControlService accessControl) {
        this.studentRepository = Objects.requireNonNull(studentRepository, "studentRepository");
        this.courseRepository = Objects.requireNonNull(courseRepository, "courseRepository");
        this.sectionRepository = Objects.requireNonNull(sectionRepository, "sectionRepository");
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository");
        this.accessControl = Objects.requireNonNull(accessControl, "accessControl");
    }

    public Student getStudentProfile(long userId) {
        accessControl.ensureLoggedIn();
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found for user: " + userId));
    }

    public List<Course> getAllCourses() {
        accessControl.ensureLoggedIn();
        return courseRepository.findAll();
    }

    public List<Course> searchCourses(String keyword) {
        accessControl.ensureLoggedIn();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCourses();
        }
        return courseRepository.searchByTitle(keyword.trim());
    }

    public List<Section> getAvailableSections(Term term, int year) {
        accessControl.ensureLoggedIn();
        return sectionRepository.findByTerm(term, year);
    }

    public List<Section> getSectionsForCourse(long courseId, Term term, int year) {
        accessControl.ensureLoggedIn();
        return sectionRepository.findByCourse(courseId, term, year);
    }

    public Enrollment registerForSection(long studentId, long sectionId) {
        accessControl.ensureWritable();
        accessControl.ensureRole(edu.univ.erp.domain.UserRole.STUDENT);

        // Verify student exists
        if (!studentRepository.findById(studentId).isPresent()) {
            throw new IllegalArgumentException("Student not found: " + studentId);
        }

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));

        // Check if already enrolled
        if (enrollmentRepository.findByStudentAndSection(studentId, sectionId).isPresent()) {
            throw new IllegalArgumentException("Already enrolled in this section.");
        }

        // Check enrollment deadline
        if (LocalDate.now().isAfter(section.enrollmentDeadline())) {
            throw new IllegalArgumentException("Enrollment deadline has passed for this section.");
        }

        // Check capacity
        int currentEnrollments = enrollmentRepository.countForSection(sectionId);
        if (currentEnrollments >= section.capacity()) {
            throw new IllegalArgumentException("Section is full. No available seats.");
        }

        // Create enrollment
        Enrollment enrollment = new Enrollment(0, studentId, sectionId,
                EnrollmentStatus.ENROLLED, null, null);
        return enrollmentRepository.save(enrollment);
    }

    public void dropSection(long studentId, long enrollmentId) {
        accessControl.ensureWritable();
        accessControl.ensureRole(edu.univ.erp.domain.UserRole.STUDENT);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + enrollmentId));

        if (enrollment.studentId() != studentId) {
            throw new AccessDeniedException("You can only drop your own enrollments.");
        }

        if (enrollment.status() != EnrollmentStatus.ENROLLED) {
            throw new IllegalArgumentException("Cannot drop a section that is not currently enrolled.");
        }

        Section section = sectionRepository.findById(enrollment.sectionId())
                .orElseThrow(() -> new IllegalArgumentException("Section not found"));

        // Check drop deadline (same as enrollment deadline for now)
        if (LocalDate.now().isAfter(section.enrollmentDeadline())) {
            throw new IllegalArgumentException("Drop deadline has passed for this section.");
        }

        enrollmentRepository.updateStatus(enrollmentId, EnrollmentStatus.DROPPED);
    }

    public List<Enrollment> getStudentEnrollments(long studentId) {
        accessControl.ensureLoggedIn();
        return enrollmentRepository.findByStudent(studentId);
    }

    public List<Section> getStudentTimetable(long studentId, Term term, int year) {
        accessControl.ensureLoggedIn();
        return sectionRepository.findStudentSections(studentId, term, year);
    }
}

