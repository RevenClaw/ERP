package edu.univ.erp.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents an offering of a course in a specific term.
 */
public final class Section {

    private final long id;
    private final long courseId;
    private final long instructorId;
    private final Term term;
    private final int year;
    private final String sectionCode;
    private final Weekday dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String room;
    private final int capacity;
    private final LocalDate enrollmentDeadline;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Section(long id,
                   long courseId,
                   long instructorId,
                   Term term,
                   int year,
                   String sectionCode,
                   Weekday dayOfWeek,
                   LocalTime startTime,
                   LocalTime endTime,
                   String room,
                   int capacity,
                   LocalDate enrollmentDeadline,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = id;
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.term = Objects.requireNonNull(term, "term");
        this.year = year;
        this.sectionCode = Objects.requireNonNull(sectionCode, "sectionCode");
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek, "dayOfWeek");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = Objects.requireNonNull(endTime, "endTime");
        this.room = Objects.requireNonNull(room, "room");
        this.capacity = capacity;
        this.enrollmentDeadline = Objects.requireNonNull(enrollmentDeadline, "enrollmentDeadline");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long id() {
        return id;
    }

    public long courseId() {
        return courseId;
    }

    public long instructorId() {
        return instructorId;
    }

    public Term term() {
        return term;
    }

    public int year() {
        return year;
    }

    public String sectionCode() {
        return sectionCode;
    }

    public Weekday dayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime startTime() {
        return startTime;
    }

    public LocalTime endTime() {
        return endTime;
    }

    public String room() {
        return room;
    }

    public int capacity() {
        return capacity;
    }

    public LocalDate enrollmentDeadline() {
        return enrollmentDeadline;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}

