package edu.univ.erp.api.admin;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.auth.AccountStatus;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Term;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.domain.Weekday;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.MaintenanceService;

/**
 * API facade for admin operations.
 */
public class AdminApi {

    private final AdminService adminService;
    private final MaintenanceService maintenanceService;

    public AdminApi(AdminService adminService,
                   MaintenanceService maintenanceService) {
        this.adminService = Objects.requireNonNull(adminService, "adminService");
        this.maintenanceService = Objects.requireNonNull(maintenanceService, "maintenanceService");
    }

    public CreateUserResult createStudent(String username, String password, String rollNumber,
                                         String program, int yearOfStudy) {
        try {
            Student student = adminService.createStudent(username, password, rollNumber, program, yearOfStudy);
            return CreateUserResult.success("Student created successfully: " + student.rollNumber());
        } catch (AccessDeniedException ex) {
            return CreateUserResult.failure(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return CreateUserResult.failure(ex.getMessage());
        } catch (Exception ex) {
            return CreateUserResult.failure("An error occurred: " + ex.getMessage());
        }
    }

    public CreateUserResult createInstructor(String username, String password, String department, String title) {
        try {
            Instructor instructor = adminService.createInstructor(username, password, department, title);
            return CreateUserResult.success("Instructor created successfully: " + instructor.department());
        } catch (AccessDeniedException ex) {
            return CreateUserResult.failure(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return CreateUserResult.failure(ex.getMessage());
        } catch (Exception ex) {
            return CreateUserResult.failure("An error occurred: " + ex.getMessage());
        }
    }

    public CreateCourseResult createCourse(String code, String title, double credits, String description) {
        try {
            Course course = adminService.createCourse(code, title, credits, description);
            return CreateCourseResult.success("Course created successfully: " + course.code());
        } catch (AccessDeniedException ex) {
            return CreateCourseResult.failure(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return CreateCourseResult.failure(ex.getMessage());
        } catch (Exception ex) {
            return CreateCourseResult.failure("An error occurred: " + ex.getMessage());
        }
    }

    public CreateSectionResult createSection(long courseId, long instructorId, Term term, int year,
                                            String sectionCode, Weekday dayOfWeek, LocalTime startTime,
                                            LocalTime endTime, String room, int capacity, LocalDate enrollmentDeadline) {
        try {
            Section section = adminService.createSection(courseId, instructorId, term, year, sectionCode,
                    dayOfWeek, startTime, endTime, room, capacity, enrollmentDeadline);
            return CreateSectionResult.success("Section created successfully: " + section.sectionCode());
        } catch (AccessDeniedException ex) {
            return CreateSectionResult.failure(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return CreateSectionResult.failure(ex.getMessage());
        } catch (Exception ex) {
            return CreateSectionResult.failure("An error occurred: " + ex.getMessage());
        }
    }

    public List<Student> getAllStudents() {
        return adminService.getAllStudents();
    }

    public List<Instructor> getAllInstructors() {
        return adminService.getAllInstructors();
    }

    public List<Course> getAllCourses() {
        return adminService.getAllCourses();
    }

    public List<Section> getAllSections() {
        return adminService.getAllSections();
    }

    public boolean isMaintenanceMode() {
        return maintenanceService.isMaintenanceMode();
    }

    public void setMaintenanceMode(boolean enabled) {
        maintenanceService.toggle(enabled);
    }

    public OperationResult updateCourse(long courseId, String code, String title, double credits, String description) {
        try {
            Course course = adminService.updateCourse(courseId, code, title, credits, description);
            return OperationResult.success("Course updated: " + course.code());
        } catch (Exception ex) {
            return OperationResult.failure(ex.getMessage());
        }
    }

    public OperationResult updateSection(long sectionId, long courseId, long instructorId, Term term, int year,
                                         String sectionCode, Weekday dayOfWeek, LocalTime startTime,
                                         LocalTime endTime, String room, int capacity, LocalDate enrollmentDeadline) {
        try {
            Section section = adminService.updateSection(sectionId, courseId, instructorId, term, year, sectionCode,
                    dayOfWeek, startTime, endTime, room, capacity, enrollmentDeadline);
            return OperationResult.success("Section updated: " + section.sectionCode());
        } catch (Exception ex) {
            return OperationResult.failure(ex.getMessage());
        }
    }

    public OperationResult updateUser(long userId,
                                      UserRole role,
                                      AccountStatus status,
                                      StudentProfilePayload studentProfile,
                                      InstructorProfilePayload instructorProfile) {
        try {
            adminService.updateUserAccount(
                    userId,
                    role,
                    status,
                    studentProfile != null
                            ? new AdminService.StudentProfileUpdate(studentProfile.rollNumber(),
                            studentProfile.program(), studentProfile.yearOfStudy())
                            : null,
                    instructorProfile != null
                            ? new AdminService.InstructorProfileUpdate(instructorProfile.department(),
                            instructorProfile.title())
                            : null
            );
            return OperationResult.success("User updated successfully.");
        } catch (AccessDeniedException | IllegalArgumentException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (Exception ex) {
            return OperationResult.failure("Failed to update user: " + ex.getMessage());
        }
    }

    public OperationResult deactivateUser(long userId) {
        try {
            adminService.deactivateUser(userId);
            return OperationResult.success("User deactivated.");
        } catch (Exception ex) {
            return OperationResult.failure(ex.getMessage());
        }
    }

    public record CreateUserResult(boolean success, String message) {
        public static CreateUserResult success(String message) {
            return new CreateUserResult(true, message);
        }

        public static CreateUserResult failure(String message) {
            return new CreateUserResult(false, message);
        }
    }

    public record CreateCourseResult(boolean success, String message) {
        public static CreateCourseResult success(String message) {
            return new CreateCourseResult(true, message);
        }

        public static CreateCourseResult failure(String message) {
            return new CreateCourseResult(false, message);
        }
    }

    public record CreateSectionResult(boolean success, String message) {
        public static CreateSectionResult success(String message) {
            return new CreateSectionResult(true, message);
        }

        public static CreateSectionResult failure(String message) {
            return new CreateSectionResult(false, message);
        }
    }

    public record OperationResult(boolean success, String message) {
        public static OperationResult success(String message) {
            return new OperationResult(true, message);
        }

        public static OperationResult failure(String message) {
            return new OperationResult(false, message);
        }
    }

    public record StudentProfilePayload(String rollNumber, String program, int yearOfStudy) {
    }

    public record InstructorProfilePayload(String department, String title) {
    }
}

