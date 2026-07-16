package com.billing.domain.enums;

final class EnumParse {

    private EnumParse() {
    }

    static <E extends Enum<E>> E required(String raw, Class<E> type, String label) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return Enum.valueOf(type, raw.trim().toUpperCase());
    }
}
