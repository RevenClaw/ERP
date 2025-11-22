package edu.univ.erp.data;

import javax.sql.DataSource;

import edu.univ.erp.config.ApplicationConfiguration;
import edu.univ.erp.config.DataSourceFactory;
import edu.univ.erp.data.jdbc.JdbcAssessmentRepository;
import edu.univ.erp.data.jdbc.JdbcAuthUserRepository;
import edu.univ.erp.data.jdbc.JdbcCourseRepository;
import edu.univ.erp.data.jdbc.JdbcEnrollmentRepository;
import edu.univ.erp.data.jdbc.JdbcFinalGradeRepository;
import edu.univ.erp.data.jdbc.JdbcGradeRepository;
import edu.univ.erp.data.jdbc.JdbcInstructorRepository;
import edu.univ.erp.data.jdbc.JdbcPasswordHistoryRepository;
import edu.univ.erp.data.jdbc.JdbcSectionRepository;
import edu.univ.erp.data.jdbc.JdbcSettingsRepository;
import edu.univ.erp.data.jdbc.JdbcStudentRepository;

/**
 * Factory for creating and wiring all repository instances with their data sources.
 */
public final class RepositoryFactory {

    private final DataSource authDataSource;
    private final DataSource erpDataSource;

    // Auth repositories
    private final AuthUserRepository authUserRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;

    // ERP repositories
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final GradeRepository gradeRepository;
    private final FinalGradeRepository finalGradeRepository;
    private final SettingsRepository settingsRepository;

    public RepositoryFactory(ApplicationConfiguration config) {
        this.authDataSource = DataSourceFactory.create(config.authDatabase(), "AuthPool");
        this.erpDataSource = DataSourceFactory.create(config.erpDatabase(), "ErpPool");

        // Initialize Auth repositories
        this.authUserRepository = new JdbcAuthUserRepository(authDataSource);
        this.passwordHistoryRepository = new JdbcPasswordHistoryRepository(authDataSource);

        // Initialize ERP repositories
        this.studentRepository = new JdbcStudentRepository(erpDataSource);
        this.instructorRepository = new JdbcInstructorRepository(erpDataSource);
        this.courseRepository = new JdbcCourseRepository(erpDataSource);
        this.sectionRepository = new JdbcSectionRepository(erpDataSource);
        this.enrollmentRepository = new JdbcEnrollmentRepository(erpDataSource);
        this.assessmentRepository = new JdbcAssessmentRepository(erpDataSource);
        this.gradeRepository = new JdbcGradeRepository(erpDataSource);
        this.finalGradeRepository = new JdbcFinalGradeRepository(erpDataSource);
        this.settingsRepository = new JdbcSettingsRepository(erpDataSource);
    }

    public AuthUserRepository authUserRepository() {
        return authUserRepository;
    }

    public PasswordHistoryRepository passwordHistoryRepository() {
        return passwordHistoryRepository;
    }

    public StudentRepository studentRepository() {
        return studentRepository;
    }

    public InstructorRepository instructorRepository() {
        return instructorRepository;
    }

    public CourseRepository courseRepository() {
        return courseRepository;
    }

    public SectionRepository sectionRepository() {
        return sectionRepository;
    }

    public EnrollmentRepository enrollmentRepository() {
        return enrollmentRepository;
    }

    public AssessmentRepository assessmentRepository() {
        return assessmentRepository;
    }

    public GradeRepository gradeRepository() {
        return gradeRepository;
    }

    public FinalGradeRepository finalGradeRepository() {
        return finalGradeRepository;
    }

    public SettingsRepository settingsRepository() {
        return settingsRepository;
    }
}

