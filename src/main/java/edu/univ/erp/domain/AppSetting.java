package edu.univ.erp.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Generic application setting key-value pair.
 */
public final class AppSetting {

    private final String key;
    private final String value;
    private final Instant updatedAt;

    public AppSetting(String key, String value, Instant updatedAt) {
        this.key = Objects.requireNonNull(key, "key");
        this.value = Objects.requireNonNull(value, "value");
        this.updatedAt = updatedAt;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}

