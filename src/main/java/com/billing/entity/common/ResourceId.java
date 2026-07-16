package com.billing.entity.common;

import java.io.Serializable;
import com.billing.exception.InvalidRequestException;

import java.util.Objects;

/**
 * Immutable value object representing a resource identifier.
 */
public final class ResourceId implements Comparable<ResourceId>, Serializable {

    private final String value;

    private ResourceId(String value) {

        if (value == null || value.isBlank()) {
            throw new InvalidRequestException("ResourceId cannot be null or blank.");
        }

        this.value = value.trim();
    }

    public static ResourceId of(String value) {
        return new ResourceId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public int compareTo(ResourceId other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof ResourceId other)) {
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