package edu.univ.erp.api.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.univ.erp.api.types.CourseRow;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.data.CourseRepository;
import edu.univ.erp.data.EnrollmentRepository;
import edu.univ.erp.data.InstructorRepository;
import edu.univ.erp.data.SectionRepository;
import edu.univ.erp.data.StudentRepository;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Term;

/**
 * API for browsing course catalog and sections.
 */
public class CatalogApi {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final InstructorRepository instructorRepository;
    private final StudentRepository studentRepository;

    public CatalogApi(CourseRepository courseRepository,
                     SectionRepository sectionRepository,
                     EnrollmentRepository enrollmentRepository,
                     InstructorRepository instructorRepository,
                     StudentRepository studentRepository) {
        this.courseRepository = Objects.requireNonNull(courseRepository, "courseRepository");
        this.sectionRepository = Objects.requireNonNull(sectionRepository, "sectionRepository");
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository");
        this.instructorRepository = Objects.requireNonNull(instructorRepository, "instructorRepository");
        this.studentRepository = Objects.requireNonNull(studentRepository, "studentRepository");
    }

    public List<CourseRow> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        List<CourseRow> rows = new ArrayList<>();
        for (Course course : courses) {
            rows.add(new CourseRow(course.id(), course.code(), course.title(),
                    course.credits(), course.description()));
        }
        return rows;
    }

    public List<CourseRow> searchCourses(String keyword) {
        List<Course> courses = courseRepository.searchByTitle(keyword);
        List<CourseRow> rows = new ArrayList<>();
        for (Course course : courses) {
            rows.add(new CourseRow(course.id(), course.code(), course.title(),
                    course.credits(), course.description()));
        }
        return rows;
    }

    public List<SectionRow> getSectionsForTerm(Term term, int year, Long studentId) {
        List<Section> sections = sectionRepository.findByTerm(term, year);
        List<SectionRow> rows = new ArrayList<>();

        for (Section section : sections) {
            Course course = courseRepository.findById(section.courseId())
                    .orElseThrow(() -> new IllegalStateException("Course not found: " + section.courseId()));

            String instructorName = instructorRepository.findById(section.instructorId())
                    .map(inst -> inst.title() + " " + inst.department())
                    .orElse("TBA");

            int enrolledCount = enrollmentRepository.countForSection(section.id());
            int availableSeats = section.capacity() - enrolledCount;
            
            // If deadline has passed, set available seats to 0
            java.time.LocalDate today = java.time.LocalDate.now();
            if (section.enrollmentDeadline().isBefore(today)) {
                availableSeats = 0;
            }

            // Convert userId to studentId if provided
            Long actualStudentId = null;
            if (studentId != null) {
                actualStudentId = studentRepository.findByUserId(studentId)
                        .map(s -> s.id())
                        .orElse(null);
            }
            
            boolean isEnrolled = actualStudentId != null
                    && enrollmentRepository.findByStudentAndSection(actualStudentId, section.id())
                            .filter(e -> e.status() == edu.univ.erp.domain.EnrollmentStatus.ENROLLED)
                            .isPresent();

            rows.add(new SectionRow(
                    section.id(),
                    section.courseId(),
                    course.code(),
                    course.title(),
                    section.sectionCode(),
                    instructorName,
                    section.term(),
                    section.year(),
                    section.dayOfWeek(),
                    section.startTime(),
                    section.endTime(),
                    section.room(),
                    section.capacity(),
                    enrolledCount,
                    availableSeats,
                    section.enrollmentDeadline(),
                    isEnrolled
            ));
        }

        return rows;
    }
}

