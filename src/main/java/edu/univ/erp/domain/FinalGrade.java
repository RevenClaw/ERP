package edu.univ.erp.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Final computed grade for an enrollment.
 */
public final class FinalGrade {

    private final long id;
    private final long enrollmentId;
    private final double finalScore;
    private final String letterGrade;
    private final Instant computedAt;

    public FinalGrade(long id,
                      long enrollmentId,
                      double finalScore,
                      String letterGrade,
                      Instant computedAt) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.finalScore = finalScore;
        this.letterGrade = Objects.requireNonNull(letterGrade, "letterGrade");
        this.computedAt = computedAt;
    }

    public long id() {
        return id;
    }

    public long enrollmentId() {
        return enrollmentId;
    }

    public double finalScore() {
        return finalScore;
    }

    public String letterGrade() {
        return letterGrade;
    }

    public Instant computedAt() {
        return computedAt;
    }
}

