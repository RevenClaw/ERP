package edu.univ.erp.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Enrollment of a student in a section.
 */
public final class Enrollment {

    private final long id;
    private final long studentId;
    private final long sectionId;
    private final EnrollmentStatus status;
    private final Instant registeredAt;
    private final Instant droppedAt;

    public Enrollment(long id,
                      long studentId,
                      long sectionId,
                      EnrollmentStatus status,
                      Instant registeredAt,
                      Instant droppedAt) {
        this.id = id;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = Objects.requireNonNull(status, "status");
        this.registeredAt = registeredAt;
        this.droppedAt = droppedAt;
    }

    public long id() {
        return id;
    }

    public long studentId() {
        return studentId;
    }

    public long sectionId() {
        return sectionId;
    }

    public EnrollmentStatus status() {
        return status;
    }

    public Instant registeredAt() {
        return registeredAt;
    }

    public Instant droppedAt() {
        return droppedAt;
    }
}

