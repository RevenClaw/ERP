package edu.univ.erp.api.types;

import java.time.LocalDate;
import java.time.LocalTime;

import edu.univ.erp.domain.Term;
import edu.univ.erp.domain.Weekday;

/**
 * Simplified section information for catalog display.
 */
public record SectionRow(
        long sectionId,
        long courseId,
        String courseCode,
        String courseTitle,
        String sectionCode,
        String instructorName,
        Term term,
        int year,
        Weekday dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String room,
        int capacity,
        int enrolledCount,
        int availableSeats,
        LocalDate enrollmentDeadline,
        boolean isEnrolled
) {
}

