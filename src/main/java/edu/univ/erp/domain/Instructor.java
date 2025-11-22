package edu.univ.erp.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a faculty member.
 */
public final class Instructor {

    private final long id;
    private final long userId;
    private final String department;
    private final String title;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Instructor(long id,
                      long userId,
                      String department,
                      String title,
                      Instant createdAt,
                      Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.department = Objects.requireNonNull(department, "department");
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long id() {
        return id;
    }

    public long userId() {
        return userId;
    }

    public String department() {
        return department;
    }

    public String title() {
        return title;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}

