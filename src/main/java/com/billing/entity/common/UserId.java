package com.billing.entity.common;

import java.io.Serializable;
import com.billing.exception.InvalidRequestException;

import java.util.Objects;

/**
 * Immutable value object representing a user identifier.
 */
public final class UserId implements Comparable<UserId>, Serializable {

    private final String value;

    private UserId(String value) {

        if (value == null || value.isBlank()) {
            throw new InvalidRequestException("UserId cannot be null or blank.");
        }

        this.value = value.trim();
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public int compareTo(UserId other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof UserId other)) {
            return false;
        }

        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}