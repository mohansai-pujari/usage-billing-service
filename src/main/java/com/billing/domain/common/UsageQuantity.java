package com.billing.domain.common;

import com.billing.exception.InvalidRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Immutable usage quantity with validated scale and arithmetic. */
public record UsageQuantity(BigDecimal value) {

    public UsageQuantity {
        if (value == null) {
            throw new InvalidRequestException("Quantity is required.");
        }
        if (value.signum() <= 0) {
            throw new InvalidRequestException("Quantity must be positive.");
        }
        if (value.scale() > ValidationRules.MAX_QUANTITY_SCALE) {
            throw new InvalidRequestException("Quantity exceeds allowed precision.");
        }
        value = value.setScale(ValidationRules.MAX_QUANTITY_SCALE, RoundingMode.HALF_UP);
    }

    public static UsageQuantity of(BigDecimal value) {
        return new UsageQuantity(value);
    }

    public static UsageQuantity of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestException("Quantity is required.");
        }
        try {
            return of(new BigDecimal(value.trim()));
        } catch (NumberFormatException ex) {
            throw new InvalidRequestException("Quantity must be a valid number.");
        }
    }

    public UsageQuantity add(UsageQuantity other) {
        return new UsageQuantity(this.value.add(other.value));
    }

    public BigDecimal asBigDecimal() {
        return value;
    }
}
