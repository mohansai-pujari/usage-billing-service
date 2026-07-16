package com.billing.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/** Supported billable services configured in {@code billing.pricing}. */
public enum ServiceType {
    STORAGE(UnitType.GB_HOUR),
    COMPUTE(UnitType.COMPUTE_HOUR),
    API(UnitType.API_CALL);

    private final UnitType expectedUnit;

    ServiceType(UnitType expectedUnit) {
        this.expectedUnit = expectedUnit;
    }

    public UnitType expectedUnit() {
        return expectedUnit;
    }

    public boolean accepts(UnitType unit) {
        return expectedUnit == unit;
    }

    @JsonCreator
    public static ServiceType fromString(String raw) {
        return EnumParse.required(raw, ServiceType.class, "Service type");
    }
}
