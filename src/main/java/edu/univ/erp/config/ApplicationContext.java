package edu.univ.erp.config;

import edu.univ.erp.access.AccessControlService;
import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.api.catalog.CatalogApi;
import edu.univ.erp.api.instructor.InstructorApi;
import edu.univ.erp.api.student.StudentApi;
import edu.univ.erp.auth.BcryptPasswordHasher;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.RepositoryFactory;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.AuthenticationService;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.service.StudentService;

/**
 * Central application context that wires all services and APIs together.
 */
public final class ApplicationContext {

    private final ApplicationConfiguration config;
    private final RepositoryFactory repositoryFactory;
    private final PasswordHasher passwordHasher;
    private final SessionManager sessionManager;
    private final AuthenticationService authenticationService;
    private final MaintenanceService maintenanceService;
    private final AccessControlService accessControlService;
    private final StudentService studentService;
    private final InstructorService instructorService;
    private final AdminService adminService;
    private final AuthApi authApi;
    private final CatalogApi catalogApi;
    private final StudentApi studentApi;
    private final InstructorApi instructorApi;
    private final AdminApi adminApi;

    public ApplicationContext() {
        this.config = ApplicationConfiguration.load();
        this.repositoryFactory = new RepositoryFactory(config);
        this.passwordHasher = new BcryptPasswordHasher();
        this.sessionManager = new SessionManager();
        this.authenticationService = new AuthenticationService(
                repositoryFactory.authUserRepository(),
                passwordHasher,
                repositoryFactory.passwordHistoryRepository());
        this.maintenanceService = new MaintenanceService(repositoryFactory.settingsRepository());
        this.accessControlService = new AccessControlService(sessionManager, maintenanceService);
        this.studentService = new StudentService(
                repositoryFactory.studentRepository(),
                repositoryFactory.courseRepository(),
                repositoryFactory.sectionRepository(),
                repositoryFactory.enrollmentRepository(),
                accessControlService);
        this.authApi = new AuthApi(authenticationService, maintenanceService, sessionManager);
        this.catalogApi = new CatalogApi(
                repositoryFactory.courseRepository(),
                repositoryFactory.sectionRepository(),
                repositoryFactory.enrollmentRepository(),
                repositoryFactory.instructorRepository(),
                repositoryFactory.studentRepository());
        this.studentApi = new StudentApi(
                studentService,
                repositoryFactory.studentRepository(),
                repositoryFactory.sectionRepository(),
                repositoryFactory.enrollmentRepository());
        this.instructorService = new InstructorService(
                repositoryFactory.sectionRepository(),
                repositoryFactory.enrollmentRepository(),
                repositoryFactory.assessmentRepository(),
                repositoryFactory.gradeRepository(),
                repositoryFactory.finalGradeRepository(),
                repositoryFactory.studentRepository(),
                accessControlService);
        this.instructorApi = new InstructorApi(
                instructorService,
                repositoryFactory.instructorRepository(),
                repositoryFactory.sectionRepository(),
                repositoryFactory.enrollmentRepository(),
                repositoryFactory.assessmentRepository(),
                repositoryFactory.gradeRepository(),
                repositoryFactory.finalGradeRepository(),
                repositoryFactory.studentRepository());
        this.adminService = new AdminService(
                repositoryFactory.authUserRepository(),
                repositoryFactory.studentRepository(),
                repositoryFactory.instructorRepository(),
                repositoryFactory.courseRepository(),
                repositoryFactory.sectionRepository(),
                accessControlService,
                passwordHasher);
        this.adminApi = new AdminApi(
                adminService,
                maintenanceService,
                repositoryFactory.authUserRepository());
    }

    public ApplicationConfiguration config() {
        return config;
    }

    public RepositoryFactory repositories() {
        return repositoryFactory;
    }

    public RepositoryFactory repositoryFactory() {
        return repositoryFactory;
    }

    public AuthApi authApi() {
        return authApi;
    }

    public SessionManager sessionManager() {
        return sessionManager;
    }

    public MaintenanceService maintenanceService() {
        return maintenanceService;
    }

    public AccessControlService accessControlService() {
        return accessControlService;
    }

    public CatalogApi catalogApi() {
        return catalogApi;
    }

    public StudentApi studentApi() {
        return studentApi;
    }

    public InstructorApi instructorApi() {
        return instructorApi;
    }

    public AdminApi adminApi() {
        return adminApi;
    }
}

