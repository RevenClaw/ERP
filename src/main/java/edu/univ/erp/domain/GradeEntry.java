package edu.univ.erp.domain;

import java.time.Instant;

/**
 * A grade awarded for a particular assessment.
 */
public final class GradeEntry {

    private final long id;
    private final long enrollmentId;
    private final long assessmentId;
    private final double score;
    private final Instant recordedAt;

    public GradeEntry(long id,
                      long enrollmentId,
                      long assessmentId,
                      double score,
                      Instant recordedAt) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.assessmentId = assessmentId;
        this.score = score;
        this.recordedAt = recordedAt;
    }

    public long id() {
        return id;
    }

    public long enrollmentId() {
        return enrollmentId;
    }

    public long assessmentId() {
        return assessmentId;
    }

    public double score() {
        return score;
    }

    public Instant recordedAt() {
        return recordedAt;
    }
}

