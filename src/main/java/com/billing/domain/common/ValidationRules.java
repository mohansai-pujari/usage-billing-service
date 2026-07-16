package com.billing.domain.common;

/** Shared validation limits and small request normalizers. */
public final class ValidationRules {

    public static final int MAX_ID_LENGTH = 128;
    public static final int MAX_QUANTITY_SCALE = 10;

    private ValidationRules() {
    }

    public static String optionalTrimmed(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
