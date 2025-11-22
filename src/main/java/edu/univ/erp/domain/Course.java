package edu.univ.erp.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a catalog course.
 */
public final class Course {

    private final long id;
    private final String code;
    private final String title;
    private final double credits;
    private final String description;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Course(long id,
                  String code,
                  String title,
                  double credits,
                  String description,
                  Instant createdAt,
                  Instant updatedAt) {
        this.id = id;
        this.code = Objects.requireNonNull(code, "code");
        this.title = Objects.requireNonNull(title, "title");
        this.credits = credits;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long id() {
        return id;
    }

    public String code() {
        return code;
    }

    public String title() {
        return title;
    }

    public double credits() {
        return credits;
    }

    public String description() {
        return description;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}

