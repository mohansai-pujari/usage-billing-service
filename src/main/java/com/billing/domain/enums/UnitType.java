package com.billing.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/** Supported units of measure configured per service in {@code billing.pricing}. */
public enum UnitType {
    GB_HOUR,
    COMPUTE_HOUR,
    API_CALL;

    @JsonCreator
    public static UnitType fromString(String raw) {
        return EnumParse.required(raw, UnitType.class, "Unit type");
    }
}
