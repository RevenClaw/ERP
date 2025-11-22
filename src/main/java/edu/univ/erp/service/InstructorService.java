package edu.univ.erp.service;

import java.util.List;
import java.util.Objects;

import edu.univ.erp.access.AccessControlService;
import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.data.AssessmentRepository;
import edu.univ.erp.data.EnrollmentRepository;
import edu.univ.erp.data.FinalGradeRepository;
import edu.univ.erp.data.GradeRepository;
import edu.univ.erp.data.SectionRepository;
import edu.univ.erp.data.StudentRepository;
import edu.univ.erp.domain.Assessment;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.FinalGrade;
import edu.univ.erp.domain.GradeEntry;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Term;

/**
 * Business logic for instructor operations.
 */
public class InstructorService {

    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final GradeRepository gradeRepository;
    private final FinalGradeRepository finalGradeRepository;
    private final StudentRepository studentRepository;
    private final AccessControlService accessControl;

    public InstructorService(SectionRepository sectionRepository,
                            EnrollmentRepository enrollmentRepository,
                            AssessmentRepository assessmentRepository,
                            GradeRepository gradeRepository,
                            FinalGradeRepository finalGradeRepository,
                            StudentRepository studentRepository,
                            AccessControlService accessControl) {
        this.sectionRepository = Objects.requireNonNull(sectionRepository, "sectionRepository");
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository");
        this.assessmentRepository = Objects.requireNonNull(assessmentRepository, "assessmentRepository");
        this.gradeRepository = Objects.requireNonNull(gradeRepository, "gradeRepository");
        this.finalGradeRepository = Objects.requireNonNull(finalGradeRepository, "finalGradeRepository");
        this.studentRepository = Objects.requireNonNull(studentRepository, "studentRepository");
        this.accessControl = Objects.requireNonNull(accessControl, "accessControl");
    }

    public List<Section> getInstructorSections(long instructorId, Term term, int year) {
        accessControl.ensureLoggedIn();
        return sectionRepository.findByInstructor(instructorId, term, year);
    }

    public List<Enrollment> getSectionEnrollments(long sectionId, long instructorId) {
        accessControl.ensureLoggedIn();
        
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));

        // Verify instructor owns this section
        if (section.instructorId() != instructorId) {
            throw new AccessDeniedException("You can only view enrollments for your own sections.");
        }

        return enrollmentRepository.findBySection(sectionId);
    }

    public List<Assessment> getSectionAssessments(long sectionId, long instructorId) {
        accessControl.ensureLoggedIn();
        
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));

        if (section.instructorId() != instructorId) {
            throw new AccessDeniedException("You can only view assessments for your own sections.");
        }

        return assessmentRepository.findBySection(sectionId);
    }

    public GradeEntry enterGrade(long sectionId, long instructorId, long enrollmentId, long assessmentId, double score) {
        accessControl.ensureWritable();
        accessControl.ensureRole(edu.univ.erp.domain.UserRole.INSTRUCTOR);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));

        if (section.instructorId() != instructorId) {
            throw new AccessDeniedException("You can only enter grades for your own sections.");
        }

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        if (assessment.sectionId() != sectionId) {
            throw new IllegalArgumentException("Assessment does not belong to this section.");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + enrollmentId));

        if (enrollment.sectionId() != sectionId) {
            throw new IllegalArgumentException("Enrollment does not belong to this section.");
        }

        // Validate score is within bounds
        if (score < 0 || score > assessment.maxScore()) {
            throw new IllegalArgumentException("Score must be between 0 and " + assessment.maxScore());
        }

        // Check if grade already exists
        GradeEntry existing = gradeRepository.findByEnrollmentAndAssessment(enrollmentId, assessmentId)
                .orElse(null);

        if (existing != null) {
            // Update existing grade
            GradeEntry updated = new GradeEntry(existing.id(), enrollmentId, assessmentId, score, existing.recordedAt());
            return gradeRepository.update(updated);
        } else {
            // Create new grade
            GradeEntry newGrade = new GradeEntry(0, enrollmentId, assessmentId, score, null);
            return gradeRepository.save(newGrade);
        }
    }

    public FinalGrade computeFinalGrade(long sectionId, long instructorId, long enrollmentId) {
        accessControl.ensureWritable();
        accessControl.ensureRole(edu.univ.erp.domain.UserRole.INSTRUCTOR);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));

        if (section.instructorId() != instructorId) {
            throw new AccessDeniedException("You can only compute final grades for your own sections.");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + enrollmentId));

        if (enrollment.sectionId() != sectionId) {
            throw new IllegalArgumentException("Enrollment does not belong to this section.");
        }

        // Get all assessments for this section
        List<Assessment> assessments = assessmentRepository.findBySection(sectionId);
        if (assessments.isEmpty()) {
            throw new IllegalStateException("No assessments defined for this section.");
        }

        // Get all grades for this enrollment
        List<GradeEntry> grades = gradeRepository.findByEnrollment(enrollmentId);

        // Compute weighted final score
        double totalWeight = 0.0;
        double weightedSum = 0.0;

        for (Assessment assessment : assessments) {
            totalWeight += assessment.weightPercent();
            
            // Find grade for this assessment
            GradeEntry grade = grades.stream()
                    .filter(g -> g.assessmentId() == assessment.id())
                    .findFirst()
                    .orElse(null);

            if (grade != null) {
                // Normalize score to percentage (0-100)
                double normalizedScore = (grade.score() / assessment.maxScore()) * 100.0;
                weightedSum += normalizedScore * (assessment.weightPercent() / 100.0);
            }
        }

        if (totalWeight == 0) {
            throw new IllegalStateException("Total assessment weight is zero.");
        }

        // Calculate final score (0-100)
        double finalScore = (weightedSum / (totalWeight / 100.0));

        // Determine letter grade
        String letterGrade = calculateLetterGrade(finalScore);

        // Save or update final grade
        FinalGrade existing = finalGradeRepository.findByEnrollment(enrollmentId).orElse(null);
        if (existing != null) {
            FinalGrade updated = new FinalGrade(existing.id(), enrollmentId, finalScore, letterGrade, existing.computedAt());
            return finalGradeRepository.update(updated);
        } else {
            FinalGrade newFinalGrade = new FinalGrade(0, enrollmentId, finalScore, letterGrade, null);
            return finalGradeRepository.save(newFinalGrade);
        }
    }

    private String calculateLetterGrade(double score) {
        if (score >= 90) return "A";
        if (score >= 85) return "A-";
        if (score >= 80) return "B+";
        if (score >= 75) return "B";
        if (score >= 70) return "B-";
        if (score >= 65) return "C+";
        if (score >= 60) return "C";
        if (score >= 55) return "C-";
        if (score >= 50) return "D";
        return "F";
    }

    public ClassStatistics getClassStatistics(long sectionId, long instructorId) {
        accessControl.ensureLoggedIn();

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));

        if (section.instructorId() != instructorId) {
            throw new AccessDeniedException("You can only view statistics for your own sections.");
        }

        List<Enrollment> enrollments = enrollmentRepository.findBySection(sectionId);
        List<Assessment> assessments = assessmentRepository.findBySection(sectionId);

        int totalStudents = enrollments.size();
        if (totalStudents == 0) {
            return new ClassStatistics(0, 0.0, 0.0, 0.0, 0.0);
        }

        // Calculate average for each assessment
        double[] assessmentAverages = new double[assessments.size()];
        for (int i = 0; i < assessments.size(); i++) {
            Assessment assessment = assessments.get(i);
            double sum = 0.0;
            int count = 0;

            for (Enrollment enrollment : enrollments) {
                GradeEntry grade = gradeRepository.findByEnrollmentAndAssessment(enrollment.id(), assessment.id())
                        .orElse(null);
                if (grade != null) {
                    sum += grade.score();
                    count++;
                }
            }

            assessmentAverages[i] = count > 0 ? (sum / count) : 0.0;
        }

        // Calculate overall average (weighted)
        double overallAverage = 0.0;
        double totalWeight = 0.0;
        for (int i = 0; i < assessments.size(); i++) {
            Assessment assessment = assessments.get(i);
            if (assessmentAverages[i] > 0) {
                double normalized = (assessmentAverages[i] / assessment.maxScore()) * 100.0;
                overallAverage += normalized * (assessment.weightPercent() / 100.0);
                totalWeight += assessment.weightPercent();
            }
        }
        overallAverage = totalWeight > 0 ? (overallAverage / (totalWeight / 100.0)) : 0.0;

        // Calculate final grade average
        double finalGradeSum = 0.0;
        int finalGradeCount = 0;
        for (Enrollment enrollment : enrollments) {
            FinalGrade finalGrade = finalGradeRepository.findByEnrollment(enrollment.id()).orElse(null);
            if (finalGrade != null) {
                finalGradeSum += finalGrade.finalScore();
                finalGradeCount++;
            }
        }
        double finalGradeAverage = finalGradeCount > 0 ? (finalGradeSum / finalGradeCount) : 0.0;

        return new ClassStatistics(totalStudents, overallAverage, finalGradeAverage, 
                assessmentAverages.length > 0 ? assessmentAverages[0] : 0.0,
                assessmentAverages.length > 1 ? assessmentAverages[1] : 0.0);
    }

    public record ClassStatistics(
            int totalStudents,
            double overallAverage,
            double finalGradeAverage,
            double firstAssessmentAverage,
            double secondAssessmentAverage
    ) {}
}

