package com.billing.domain.common;

/** Shared validation limits applied across domain types and API request models. */
public final class ValidationRules {

    public static final int MAX_ID_LENGTH = 128;
    public static final int MAX_QUANTITY_SCALE = 10;

    private ValidationRules() {
    }
}
