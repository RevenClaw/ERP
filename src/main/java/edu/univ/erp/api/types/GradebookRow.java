package edu.univ.erp.api.types;

import java.util.Map;

/**
 * Represents a student's gradebook entry for a section.
 */
public record GradebookRow(
        long enrollmentId,
        long studentId,
        String studentName,
        String rollNo,
        Map<Long, Double> assessmentScores, // assessmentId -> score
        Double finalScore,
        String letterGrade
) {
}

