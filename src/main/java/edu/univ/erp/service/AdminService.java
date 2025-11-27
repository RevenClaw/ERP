package edu.univ.erp.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import edu.univ.erp.access.AccessControlService;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.data.AuthUserRepository;
import edu.univ.erp.data.CourseRepository;
import edu.univ.erp.data.InstructorRepository;
import edu.univ.erp.data.SectionRepository;
import edu.univ.erp.data.StudentRepository;
import edu.univ.erp.auth.AccountStatus;
import edu.univ.erp.auth.AuthUser;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.StudentStatus;
import edu.univ.erp.domain.Term;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.domain.Weekday;

/**
 * Business logic for admin operations.
 */
public class AdminService {

    private final AuthUserRepository authUserRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final AccessControlService accessControl;
    private final PasswordHasher passwordHasher;

    public AdminService(AuthUserRepository authUserRepository,
                       StudentRepository studentRepository,
                       InstructorRepository instructorRepository,
                       CourseRepository courseRepository,
                       SectionRepository sectionRepository,
                       AccessControlService accessControl,
                       PasswordHasher passwordHasher) {
        this.authUserRepository = Objects.requireNonNull(authUserRepository, "authUserRepository");
        this.studentRepository = Objects.requireNonNull(studentRepository, "studentRepository");
        this.instructorRepository = Objects.requireNonNull(instructorRepository, "instructorRepository");
        this.courseRepository = Objects.requireNonNull(courseRepository, "courseRepository");
        this.sectionRepository = Objects.requireNonNull(sectionRepository, "sectionRepository");
        this.accessControl = Objects.requireNonNull(accessControl, "accessControl");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
    }

    public Student createStudent(String username, String password, String rollNumber, String program, int yearOfStudy) {
        accessControl.ensureWritable();
        accessControl.ensureRole(UserRole.ADMIN);

        // Check if username already exists
        if (authUserRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        // Create auth user
        String passwordHash = passwordHasher.hash(password.toCharArray());
        AuthUser authUser = new AuthUser(0, username, UserRole.STUDENT, AccountStatus.ACTIVE,
                passwordHash, 0, null);
        AuthUser savedAuthUser = authUserRepository.save(authUser);

        // Create student profile
        Student student = new Student(0, savedAuthUser.id(), rollNumber, program, yearOfStudy,
                StudentStatus.ACTIVE, null, null);
        return studentRepository.save(student);
    }

    public Instructor createInstructor(String username, String password, String department, String title) {
        accessControl.ensureWritable();
        accessControl.ensureRole(UserRole.ADMIN);

        // Check if username already exists
        if (authUserRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        // Create auth user
        String passwordHash = passwordHasher.hash(password.toCharArray());
        AuthUser authUser = new AuthUser(0, username, UserRole.INSTRUCTOR, AccountStatus.ACTIVE,
                passwordHash, 0, null);
        AuthUser savedAuthUser = authUserRepository.save(authUser);

        // Create instructor profile
        Instructor instructor = new Instructor(0, savedAuthUser.id(), department, title, null, null);
        return instructorRepository.save(instructor);
    }

    public Course createCourse(String code, String title, double credits, String description) {
        accessControl.ensureWritable();
        accessControl.ensureRole(UserRole.ADMIN);

        // Check if course code already exists
        if (courseRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Course code already exists: " + code);
        }

        Course course = new Course(0, code, title, credits, description, null, null);
        return courseRepository.save(course);
    }

    public Course updateCourse(long courseId, String code, String title, double credits, String description) {
        accessControl.ensureWritable();
        accessControl.ensureRole(UserRole.ADMIN);

        Course existing = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        // Check if new code conflicts with another course
        if (!existing.code().equals(code)) {
            if (courseRepository.findByCode(code).isPresent()) {
                throw new IllegalArgumentException("Course code already exists: " + code);
            }
        }

        Course updated = new Course(courseId, code, title, credits, description,
                existing.createdAt(), null);
        return courseRepository.update(updated);
    }

    public Section createSection(long courseId, long instructorId, Term term, int year,
                                String sectionCode, Weekday dayOfWeek, LocalTime startTime,
                                LocalTime endTime, String room, int capacity, LocalDate enrollmentDeadline) {
        accessControl.ensureWritable();
        accessControl.ensureRole(UserRole.ADMIN);

        // Validate course exists
        courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        // Validate instructor exists
        instructorRepository.findById(instructorId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found: " + instructorId));

        // Validate capacity
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        Section section = new Section(0, courseId, instructorId, term, year, sectionCode,
                dayOfWeek, startTime, endTime, room, capacity, enrollmentDeadline, null, null);
        return sectionRepository.save(section);
    }

    public Section updateSection(long sectionId, long courseId, long instructorId, Term term, int year,
                                String sectionCode, Weekday dayOfWeek, LocalTime startTime,
                                LocalTime endTime, String room, int capacity, LocalDate enrollmentDeadline) {
        accessControl.ensureWritable();
        accessControl.ensureRole(UserRole.ADMIN);

        Section existing = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));

        // Validate course exists
        courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        // Validate instructor exists
        instructorRepository.findById(instructorId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found: " + instructorId));

        // Validate capacity
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        Section updated = new Section(sectionId, courseId, instructorId, term, year, sectionCode,
                dayOfWeek, startTime, endTime, room, capacity, enrollmentDeadline,
                existing.createdAt(), null);
        return sectionRepository.update(updated);
    }

    public List<Student> getAllStudents() {
        accessControl.ensureLoggedIn();
        accessControl.ensureRole(UserRole.ADMIN);
        return studentRepository.findAll();
    }

    public List<Instructor> getAllInstructors() {
        accessControl.ensureLoggedIn();
        accessControl.ensureRole(UserRole.ADMIN);
        return instructorRepository.findAll();
    }

    public List<Course> getAllCourses() {
        accessControl.ensureLoggedIn();
        accessControl.ensureRole(UserRole.ADMIN);
        return courseRepository.findAll();
    }

    public List<Section> getAllSections() {
        accessControl.ensureLoggedIn();
        accessControl.ensureRole(UserRole.ADMIN);
        return sectionRepository.findAll();
    }

    public AuthUser updateUserAccount(long userId,
                                      UserRole newRole,
                                      AccountStatus status,
                                      StudentProfileUpdate studentProfile,
                                      InstructorProfileUpdate instructorProfile) {
        accessControl.ensureWritable();
        accessControl.ensureRole(UserRole.ADMIN);

        AuthUser authUser = authUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (authUser.status() != status) {
            authUser = authUserRepository.updateStatus(userId, status);
        }

        if (authUser.role() != newRole) {
            authUser = authUserRepository.updateRole(userId, newRole);
        }

        switch (newRole) {
            case STUDENT -> {
                if (studentProfile == null) {
                    throw new IllegalArgumentException("Student details are required for student role.");
                }
                var existingStudent = studentRepository.findByUserId(userId);
                if (existingStudent.isPresent()) {
                    Student current = existingStudent.get();
                    Student updated = new Student(current.id(), userId, studentProfile.rollNumber(),
                            studentProfile.program(), studentProfile.yearOfStudy(), current.status(),
                            current.createdAt(), current.updatedAt());
                    studentRepository.update(updated);
                } else {
                    Student created = new Student(0, userId, studentProfile.rollNumber(),
                            studentProfile.program(), studentProfile.yearOfStudy(), StudentStatus.ACTIVE, null, null);
                    studentRepository.save(created);
                }
                instructorRepository.deleteByUserId(userId);
            }
            case INSTRUCTOR -> {
                if (instructorProfile == null) {
                    throw new IllegalArgumentException("Instructor details are required for instructor role.");
                }
                var existingInstructor = instructorRepository.findByUserId(userId);
                if (existingInstructor.isPresent()) {
                    Instructor current = existingInstructor.get();
                    Instructor updated = new Instructor(current.id(), userId,
                            instructorProfile.department(), instructorProfile.title(),
                            current.createdAt(), current.updatedAt());
                    instructorRepository.update(updated);
                } else {
                    Instructor created = new Instructor(0, userId, instructorProfile.department(),
                            instructorProfile.title(), null, null);
                    instructorRepository.save(created);
                }
                studentRepository.deleteByUserId(userId);
            }
            case ADMIN -> {
                studentRepository.deleteByUserId(userId);
                instructorRepository.deleteByUserId(userId);
            }
        }

        return authUser;
    }

    public AuthUser deactivateUser(long userId) {
        accessControl.ensureWritable();
        accessControl.ensureRole(UserRole.ADMIN);
        authUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return authUserRepository.updateStatus(userId, AccountStatus.DISABLED);
    }

    public record StudentProfileUpdate(String rollNumber, String program, int yearOfStudy) {
    }

    public record InstructorProfileUpdate(String department, String title) {
    }
}

