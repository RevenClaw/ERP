package edu.univ.erp.api.instructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.api.types.GradebookRow;
import edu.univ.erp.data.FinalGradeRepository;
import edu.univ.erp.data.GradeRepository;
import edu.univ.erp.data.InstructorRepository;
import edu.univ.erp.data.StudentRepository;
import edu.univ.erp.domain.Assessment;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.FinalGrade;
import edu.univ.erp.domain.GradeEntry;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Term;
import edu.univ.erp.service.InstructorService;

/**
 * API facade for instructor operations.
 */
public class InstructorApi {

    private final InstructorService instructorService;
    private final InstructorRepository instructorRepository;
    private final GradeRepository gradeRepository;
    private final FinalGradeRepository finalGradeRepository;
    private final StudentRepository studentRepository;

    public InstructorApi(InstructorService instructorService,
                        InstructorRepository instructorRepository,
                        GradeRepository gradeRepository,
                        FinalGradeRepository finalGradeRepository,
                        StudentRepository studentRepository) {
        this.instructorService = Objects.requireNonNull(instructorService, "instructorService");
        this.instructorRepository = Objects.requireNonNull(instructorRepository, "instructorRepository");
        this.gradeRepository = Objects.requireNonNull(gradeRepository, "gradeRepository");
        this.finalGradeRepository = Objects.requireNonNull(finalGradeRepository, "finalGradeRepository");
        this.studentRepository = Objects.requireNonNull(studentRepository, "studentRepository");
    }

    public Instructor getInstructorProfile(long userId) {
        return instructorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));
    }

    public List<Section> getMySections(long userId, Term term, int year) {
        Instructor instructor = instructorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));
        return instructorService.getInstructorSections(instructor.id(), term, year);
    }

    public List<GradebookRow> getGradebook(long userId, long sectionId) {
        Instructor instructor = instructorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));

        List<Enrollment> enrollments = instructorService.getSectionEnrollments(sectionId, instructor.id());
        List<Assessment> assessments = instructorService.getSectionAssessments(sectionId, instructor.id());
        List<GradebookRow> rows = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            if (enrollment.status() != edu.univ.erp.domain.EnrollmentStatus.ENROLLED) {
                continue;
            }

            Student student = studentRepository.findById(enrollment.studentId())
                    .orElseThrow(() -> new IllegalStateException("Student not found"));

            // Get all grades for this enrollment
            Map<Long, Double> assessmentScores = new HashMap<>();
            for (Assessment assessment : assessments) {
                GradeEntry grade = gradeRepository.findByEnrollmentAndAssessment(enrollment.id(), assessment.id())
                        .orElse(null);
                if (grade != null) {
                    assessmentScores.put(assessment.id(), grade.score());
                }
            }

            // Get final grade
            FinalGrade finalGrade = finalGradeRepository.findByEnrollment(enrollment.id()).orElse(null);
            Double finalScore = finalGrade != null ? finalGrade.finalScore() : null;
            String letterGrade = finalGrade != null ? finalGrade.letterGrade() : null;

            rows.add(new GradebookRow(
                    enrollment.id(),
                    student.id(),
                    student.rollNumber(),
                    student.rollNumber(),
                    assessmentScores,
                    finalScore,
                    letterGrade
            ));
        }

        return rows;
    }

    public EnterGradeResult enterGrade(long userId, long sectionId, long enrollmentId, long assessmentId, double score) {
        try {
            Instructor instructor = instructorRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));

            instructorService.enterGrade(sectionId, instructor.id(), enrollmentId, assessmentId, score);
            return EnterGradeResult.success("Grade entered successfully.");
        } catch (AccessDeniedException ex) {
            return EnterGradeResult.failure(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return EnterGradeResult.failure(ex.getMessage());
        } catch (Exception ex) {
            return EnterGradeResult.failure("An error occurred: " + ex.getMessage());
        }
    }

    public ComputeFinalGradeResult computeFinalGrade(long userId, long sectionId, long enrollmentId) {
        try {
            Instructor instructor = instructorRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));

            FinalGrade finalGrade = instructorService.computeFinalGrade(sectionId, instructor.id(), enrollmentId);
            return ComputeFinalGradeResult.success("Final grade computed: " + finalGrade.letterGrade() + " (" + 
                    String.format("%.2f", finalGrade.finalScore()) + "%)");
        } catch (AccessDeniedException ex) {
            return ComputeFinalGradeResult.failure(ex.getMessage());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ComputeFinalGradeResult.failure(ex.getMessage());
        } catch (Exception ex) {
            return ComputeFinalGradeResult.failure("An error occurred: " + ex.getMessage());
        }
    }

    public InstructorService.ClassStatistics getClassStatistics(long userId, long sectionId) {
        Instructor instructor = instructorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));
        return instructorService.getClassStatistics(sectionId, instructor.id());
    }

    public ManageWeightsResult updateAssessmentWeights(long userId, long sectionId, Map<Long, Double> weights) {
        try {
            Instructor instructor = instructorRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));
            instructorService.updateAssessmentWeights(sectionId, instructor.id(), weights);
            return ManageWeightsResult.success("Assessment weights updated.");
        } catch (AccessDeniedException | IllegalArgumentException ex) {
            return ManageWeightsResult.failure(ex.getMessage());
        } catch (Exception ex) {
            return ManageWeightsResult.failure("Failed to update weights: " + ex.getMessage());
        }
    }

    public record EnterGradeResult(boolean success, String message) {
        public static EnterGradeResult success(String message) {
            return new EnterGradeResult(true, message);
        }

        public static EnterGradeResult failure(String message) {
            return new EnterGradeResult(false, message);
        }
    }

    public record ComputeFinalGradeResult(boolean success, String message) {
        public static ComputeFinalGradeResult success(String message) {
            return new ComputeFinalGradeResult(true, message);
        }

        public static ComputeFinalGradeResult failure(String message) {
            return new ComputeFinalGradeResult(false, message);
        }
    }

    public record ManageWeightsResult(boolean success, String message) {
        public static ManageWeightsResult success(String message) {
            return new ManageWeightsResult(true, message);
        }

        public static ManageWeightsResult failure(String message) {
            return new ManageWeightsResult(false, message);
        }
    }
}

