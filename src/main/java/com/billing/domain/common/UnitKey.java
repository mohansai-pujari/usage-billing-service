package com.billing.domain.common;

import com.billing.exception.InvalidRequestException;

/** Normalized unit of measure (e.g. {@code GB_HOUR}, {@code API_CALL}). */
public record UnitKey(String value) {

    public UnitKey {
        value = normalize(value, "Unit");
    }

    public static UnitKey of(String value) {
        return new UnitKey(value);
    }

    static String normalize(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidRequestException(fieldName + " is required.");
        }
        return raw.trim().toUpperCase();
    }
}
