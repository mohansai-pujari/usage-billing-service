package com.billing.domain.common;

import com.billing.domain.enums.CurrencyType;
import com.billing.exception.InvalidRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Immutable monetary amount in USD using {@link BigDecimal} (never floating-point). */
public final class Money {

    private static final int DISPLAY_SCALE = 2;

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidRequestException("Monetary amount cannot be null.");
        }
        this.amount = amount;
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public BigDecimal amount() {
        return amount;
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "Money operand cannot be null.");
        return rounded(amount.add(other.amount));
    }

    public Money multiply(BigDecimal multiplier) {
        if (multiplier == null) {
            throw new InvalidRequestException("Multiplier cannot be null.");
        }
        return rounded(amount.multiply(multiplier));
    }

    public Money subtract(Money other) {
        Objects.requireNonNull(other, "Money operand cannot be null.");
        return rounded(amount.subtract(other.amount));
    }

    public static Money proportionalShare(Money total, BigDecimal part, BigDecimal whole) {
        if (whole == null || whole.signum() == 0) {
            return zero();
        }
        if (part == null) {
            throw new InvalidRequestException("Part quantity cannot be null.");
        }
        return rounded(total.amount().multiply(part).divide(whole, DISPLAY_SCALE + 4, RoundingMode.HALF_UP));
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    private static Money rounded(BigDecimal value) {
        return new Money(value.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Money other)) {
            return false;
        }
        return amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }

    public String format(CurrencyType currency) {
        CurrencyType displayCurrency = currency != null ? currency : CurrencyType.USD;
        return displayCurrency.name() + " "
                + amount.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    public String toString() {
        return format(CurrencyType.USD);
    }
}
