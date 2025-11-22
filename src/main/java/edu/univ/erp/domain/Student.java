package edu.univ.erp.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents an enrolled student.
 */
public final class Student {

    private final long id;
    private final long userId;
    private final String rollNumber;
    private final String program;
    private final int yearOfStudy;
    private final StudentStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Student(long id,
                   long userId,
                   String rollNumber,
                   String program,
                   int yearOfStudy,
                   StudentStatus status,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.rollNumber = Objects.requireNonNull(rollNumber, "rollNumber");
        this.program = Objects.requireNonNull(program, "program");
        this.yearOfStudy = yearOfStudy;
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long id() {
        return id;
    }

    public long userId() {
        return userId;
    }

    public String rollNumber() {
        return rollNumber;
    }

    public String program() {
        return program;
    }

    public int yearOfStudy() {
        return yearOfStudy;
    }

    public StudentStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}

