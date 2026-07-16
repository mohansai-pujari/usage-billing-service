package com.billing.entity.common;

import com.billing.exception.InvalidRequestException;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a billing period using epoch-millisecond timestamps.
 *
 * The period is represented as:
 *
 * [start, end)
 *
 * Start is inclusive.
 * End is exclusive.
 */
public final class BillingPeriod {

    private final long startTimestamp;

    private final long endTimestamp;

    public BillingPeriod(long startTimestamp, long endTimestamp) {

        if (startTimestamp >= endTimestamp) {
            throw new InvalidRequestException("Billing period start must be before end.");
        }

        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public long getStart() {
        return startTimestamp;
    }

    public long getEnd() {
        return endTimestamp;
    }

    /**
     * Checks whether a timestamp belongs to this billing period.
     *
     * Period semantics:
     * [start, end)
     */
    public boolean contains(Instant timestamp) {

        Objects.requireNonNull(timestamp, "Timestamp cannot be null.");

        long eventTimestamp = timestamp.toEpochMilli();
        return eventTimestamp >= startTimestamp && eventTimestamp < endTimestamp;
    }

    @Override
    public String toString() {
        return "BillingPeriod{" +
                "start=" + startTimestamp +
                ", end=" + endTimestamp +
                '}';
    }
}
