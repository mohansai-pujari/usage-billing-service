package com.billing.entity.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key representing a user's usage of a resource.
 */
public final class UsageKey implements Comparable<UsageKey>, Serializable {

    private final UserId userId;
    private final ResourceId resourceId;

    private UsageKey(UserId userId, ResourceId resourceId) {

        this.userId = Objects.requireNonNull(userId);
        this.resourceId = Objects.requireNonNull(resourceId);
    }

    public static UsageKey of(UserId userId, ResourceId resourceId) {

        return new UsageKey(userId, resourceId);
    }

    public UserId getUserId() {
        return userId;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public int compareTo(UsageKey other) {

        int result = userId.compareTo(other.userId);

        if (result != 0) {
            return result;
        }

        return resourceId.compareTo(other.resourceId);
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof UsageKey other)) {
            return false;
        }

        return userId.equals(other.userId) && resourceId.equals(other.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, resourceId);
    }

    @Override
    public String toString() {
        return userId + ":" + resourceId;
    }
}