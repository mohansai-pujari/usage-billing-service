package com.billing.domain.common;

import com.billing.exception.InvalidRequestException;

/** Billing period as epoch ms UTC: [start, end). */
public record BillingPeriod(long start, long end) {

    public BillingPeriod {
        if (start < 0 || end < 0) {
            throw new InvalidRequestException("Billing period timestamps must be non-negative.");
        }
        if (start >= end) {
            throw new InvalidRequestException("Billing period start must be before end.");
        }
    }

    public boolean contains(long timestamp) {
        if (timestamp < 0) {
            throw new InvalidRequestException("Timestamp must be non-negative.");
        }
        return timestamp >= start && timestamp < end;
    }
}
