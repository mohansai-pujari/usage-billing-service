package com.billing.entity.common;

import com.billing.exception.InvalidRequestException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable value object representing resource usage quantity.
 * <p>
 * Examples:
 * - 10 GB-HOUR
 * - 150 COMPUTE-HOUR
 * - 2_000_000 API-CALL
 * <p>
 * Unit information is intentionally NOT stored here.
 * The associated UnitType is maintained by UsageEvent/UsageSummary.
 */
public final class UsageQuantity implements Comparable<UsageQuantity>, Serializable {

    private static final int SCALE = 6;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static final UsageQuantity ZERO = new UsageQuantity(BigDecimal.ZERO);

    private final BigDecimal value;

    private UsageQuantity(BigDecimal value) {

        Objects.requireNonNull(value, "Usage quantity cannot be null.");

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("Usage quantity cannot be negative.");
        }

        this.value = value.setScale(SCALE, ROUNDING_MODE);
    }

    public static UsageQuantity of(BigDecimal value) {
        return new UsageQuantity(value);
    }

    public static UsageQuantity of(String value) {
        return new UsageQuantity(new BigDecimal(value));
    }

    public static UsageQuantity of(long value) {
        return new UsageQuantity(BigDecimal.valueOf(value));
    }

    public BigDecimal value() {
        return value;
    }

    public UsageQuantity add(UsageQuantity other) {

        Objects.requireNonNull(other);

        return new UsageQuantity(value.add(other.value));
    }

    public UsageQuantity subtract(UsageQuantity other) {

        Objects.requireNonNull(other);

        BigDecimal result = value.subtract(other.value);

        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("Resulting usage quantity cannot be negative.");
        }

        return new UsageQuantity(result);
    }

    public UsageQuantity multiply(BigDecimal multiplier) {

        Objects.requireNonNull(multiplier);

        return new UsageQuantity(value.multiply(multiplier));
    }

    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public int compareTo(UsageQuantity other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof UsageQuantity other)) {
            return false;
        }

        return value.compareTo(other.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return value.stripTrailingZeros().toPlainString();
    }
}