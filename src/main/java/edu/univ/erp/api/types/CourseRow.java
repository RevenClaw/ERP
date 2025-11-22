package edu.univ.erp.api.types;

/**
 * Simplified course information for catalog display.
 */
public record CourseRow(
        long courseId,
        String code,
        String title,
        double credits,
        String description
) {
}

