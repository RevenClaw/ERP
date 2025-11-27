package edu.univ.erp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.univ.erp.access.AccessControlService;
import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.data.AssessmentRepository;
import edu.univ.erp.data.EnrollmentRepository;
import edu.univ.erp.data.FinalGradeRepository;
import edu.univ.erp.data.GradeRepository;
import edu.univ.erp.data.SectionRepository;
import edu.univ.erp.domain.Assessment;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.FinalGrade;
import edu.univ.erp.domain.GradeEntry;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Term;

/**
 * Business logic for instructor operations.
 */
public class InstructorService {

    private static final List<DefaultAssessmentSpec> DEFAULT_ASSESSMENTS = List.of(
            new DefaultAssessmentSpec("Quiz", 20.0, 20.0),
            new DefaultAssessmentSpec("Midterm", 30.0, 30.0),
            new DefaultAssessmentSpec("Endsem", 50.0, 50.0)
    );

    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final GradeRepository gradeRepository;
    private final FinalGradeRepository finalGradeRepository;
    private final AccessControlService accessControl;

    public InstructorService(SectionRepository sectionRepository,
                            EnrollmentRepository enrollmentRepository,
                            AssessmentRepository assessmentRepository,
                            GradeRepository gradeRepository,
                            FinalGradeRepository finalGradeRepository,
                            AccessControlService accessControl) {
        this.sectionRepository = Objects.requireNonNull(sectionRepository, "sectionRepository");
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository");
        this.assessmentRepository = Objects.requireNonNull(assessmentRepository, "assessmentRepository");
        this.gradeRepository = Objects.requireNonNull(gradeRepository, "gradeRepository");
        this.finalGradeRepository = Objects.requireNonNull(finalGradeRepository, "finalGradeRepository");
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

        return ensureAssessments(sectionId);
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
        List<Assessment> assessments = ensureAssessments(sectionId);

        // Get all grades for this enrollment
        List<GradeEntry> grades = gradeRepository.findByEnrollment(enrollmentId);

        double finalScore = calculateWeightedScore(assessments, grades);
        if (Double.isNaN(finalScore)) {
            throw new IllegalStateException("Total assessment weight is zero.");
        }

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
        List<Assessment> assessments = ensureAssessments(sectionId);

        int totalStudents = enrollments.size();
        if (totalStudents == 0 || assessments.isEmpty()) {
            return new ClassStatistics(0, 0.0, 0.0, 0.0, 0.0);
        }

        List<Double> finalScores = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            List<GradeEntry> grades = gradeRepository.findByEnrollment(enrollment.id());
            double score = calculateWeightedScore(assessments, grades);
            if (!Double.isNaN(score)) {
                finalScores.add(score);
            }
        }

        if (finalScores.isEmpty()) {
            return new ClassStatistics(totalStudents, 0.0, 0.0, 0.0, 0.0);
        }

        Collections.sort(finalScores);
        double average = finalScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double median = finalScores.size() % 2 == 0
                ? (finalScores.get(finalScores.size() / 2 - 1) + finalScores.get(finalScores.size() / 2)) / 2.0
                : finalScores.get(finalScores.size() / 2);
        double lowest = finalScores.get(0);
        double highest = finalScores.get(finalScores.size() - 1);

        return new ClassStatistics(totalStudents, average, median, highest, lowest);
    }

    public void updateAssessmentWeights(long sectionId,
                                        long instructorId,
                                        Map<Long, Double> weights) {
        accessControl.ensureWritable();
        accessControl.ensureRole(edu.univ.erp.domain.UserRole.INSTRUCTOR);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));

        if (section.instructorId() != instructorId) {
            throw new AccessDeniedException("You can only configure weights for your own sections.");
        }

        List<Assessment> assessments = ensureAssessments(sectionId);

        double totalWeight = 0.0;
        for (Assessment assessment : assessments) {
            Double newWeight = weights.get(assessment.id());
            if (newWeight == null) {
                throw new IllegalArgumentException("Missing weight for assessment: " + assessment.name());
            }
            if (newWeight < 0 || newWeight > 100) {
                throw new IllegalArgumentException("Weights must be between 0 and 100.");
            }
            totalWeight += newWeight;
        }

        if (Math.abs(totalWeight - 100.0) > 0.01) {
            throw new IllegalArgumentException("Total weight must equal 100%.");
        }

        for (Assessment assessment : assessments) {
            double updatedWeight = weights.get(assessment.id());
            Assessment updated = new Assessment(
                    assessment.id(),
                    assessment.sectionId(),
                    assessment.name(),
                    updatedWeight,
                    assessment.maxScore(),
                    assessment.createdAt(),
                    assessment.updatedAt()
            );
            assessmentRepository.update(updated);
        }
    }

    public record ClassStatistics(
            int totalStudents,
            double average,
            double median,
            double highest,
            double lowest
    ) {}

    private record DefaultAssessmentSpec(String name, double weight, double maxScore) {}

    private double calculateWeightedScore(List<Assessment> assessments, List<GradeEntry> grades) {
        double totalWeight = assessments.stream()
                .mapToDouble(Assessment::weightPercent)
                .sum();
        if (totalWeight == 0) {
            return Double.NaN;
        }

        double weightedSum = 0.0;
        for (Assessment assessment : assessments) {
            GradeEntry grade = grades.stream()
                    .filter(g -> g.assessmentId() == assessment.id())
                    .findFirst()
                    .orElse(null);
            if (grade != null) {
                double normalizedScore = (grade.score() / assessment.maxScore()) * 100.0;
                weightedSum += normalizedScore * (assessment.weightPercent() / 100.0);
            }
        }

        return weightedSum / (totalWeight / 100.0);
    }

    private List<Assessment> ensureAssessments(long sectionId) {
        List<Assessment> assessments = assessmentRepository.findBySection(sectionId);
        if (!assessments.isEmpty()) {
            return assessments;
        }
        for (DefaultAssessmentSpec spec : DEFAULT_ASSESSMENTS) {
            Assessment assessment = new Assessment(0, sectionId, spec.name(), spec.weight(), spec.maxScore(), null, null);
            assessmentRepository.save(assessment);
        }
        return assessmentRepository.findBySection(sectionId);
    }
}

