package com.billing.domain.common;

import com.billing.exception.InvalidRequestException;

import java.time.Instant;

/**
 * Immutable billing time window using epoch-millisecond timestamps.
 * Semantics: {@code [start, end)} — start inclusive, end exclusive.
 */
public record BillingPeriod(long start, long end) {

    public BillingPeriod {
        if (start < 0 || end < 0) {
            throw new InvalidRequestException("Billing period timestamps must be non-negative.");
        }
        if (start >= end) {
            throw new InvalidRequestException("Billing period start must be before end.");
        }
    }

    public boolean contains(Instant timestamp) {
        if (timestamp == null) {
            throw new InvalidRequestException("Timestamp cannot be null.");
        }
        long value = timestamp.toEpochMilli();
        return value >= start && value < end;
    }
}
