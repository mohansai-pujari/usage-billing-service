package com.billing.domain.common;

import com.billing.exception.InvalidRequestException;

/** Normalized service identifier from configuration (e.g. {@code storage}, {@code compute}). */
public record ServiceKey(String value) {

    public ServiceKey {
        value = normalize(value, "Service type");
    }

    public static ServiceKey of(String value) {
        return new ServiceKey(value);
    }

    static String normalize(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidRequestException(fieldName + " is required.");
        }
        return raw.trim().toLowerCase();
    }
}
