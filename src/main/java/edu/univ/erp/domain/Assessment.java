package edu.univ.erp.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Describes a graded assessment component.
 */
public final class Assessment {

    private final long id;
    private final long sectionId;
    private final String name;
    private final double weightPercent;
    private final double maxScore;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Assessment(long id,
                      long sectionId,
                      String name,
                      double weightPercent,
                      double maxScore,
                      Instant createdAt,
                      Instant updatedAt) {
        this.id = id;
        this.sectionId = sectionId;
        this.name = Objects.requireNonNull(name, "name");
        this.weightPercent = weightPercent;
        this.maxScore = maxScore;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long id() {
        return id;
    }

    public long sectionId() {
        return sectionId;
    }

    public String name() {
        return name;
    }

    public double weightPercent() {
        return weightPercent;
    }

    public double maxScore() {
        return maxScore;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}

