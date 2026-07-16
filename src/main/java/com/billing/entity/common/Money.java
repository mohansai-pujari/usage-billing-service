package com.billing.entity.common;

import com.billing.constants.ApplicationConstants;
import com.billing.enums.CurrencyType;
import com.billing.exception.InvalidRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable Value Object representing a monetary amount.
 *
 * All arithmetic operations require matching currencies.
 * Currency conversion is handled outside this class.
 */
public final class Money implements Comparable<Money> {

    private final BigDecimal amount;
    private final CurrencyType currency;

    private Money(BigDecimal amount, CurrencyType currency) {

        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        this.amount = amount.setScale(
                ApplicationConstants.MONEY_SCALE,
                RoundingMode.HALF_UP);

        this.currency = currency;
    }

    /**
     * Creates Money using default billing currency (USD).
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount, CurrencyType.USD);
    }

    /**
     * Creates Money using specified currency.
     */
    public static Money of(BigDecimal amount,
                           CurrencyType currency) {
        return new Money(amount, currency);
    }

    /**
     * Convenience factory method.
     */
    public static Money of(String amount) {
        return of(new BigDecimal(amount));
    }

    /**
     * Convenience factory method.
     */
    public static Money of(String amount,
                           CurrencyType currency) {
        return of(new BigDecimal(amount), currency);
    }

    /**
     * Convenience factory method.
     */
    public static Money of(long amount) {
        return of(BigDecimal.valueOf(amount));
    }

    /**
     * Returns zero in USD.
     */
    public static Money zero() {
        return new Money(
                BigDecimal.ZERO,
                CurrencyType.USD
        );
    }

    /**
     * Returns zero in specified currency.
     */
    public static Money zero(CurrencyType currency) {
        return new Money(
                BigDecimal.ZERO,
                currency
        );
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    /**
     * Adds another Money amount.
     */
    public Money add(Money other) {

        validateCurrency(other);

        return new Money(
                amount.add(other.amount),
                currency
        );
    }

    /**
     * Subtracts another Money amount.
     */
    public Money subtract(Money other) {

        validateCurrency(other);

        return new Money(
                amount.subtract(other.amount),
                currency
        );
    }

    /**
     * Multiplies by quantity.
     */
    public Money multiply(BigDecimal multiplier) {

        Objects.requireNonNull(
                multiplier,
                "Multiplier cannot be null"
        );

        return new Money(
                amount.multiply(multiplier),
                currency
        );
    }

    /**
     * Divides amount.
     */
    public Money divide(BigDecimal divisor) {

        Objects.requireNonNull(
                divisor,
                "Divisor cannot be null"
        );

        return new Money(
                amount.divide(
                        divisor,
                        ApplicationConstants.MONEY_SCALE,
                        RoundingMode.HALF_UP
                ),
                currency
        );
    }

    /**
     * Returns true if amount is zero.
     */
    public boolean isZero() {
        return amount.signum() == 0;
    }

    /**
     * Returns true if amount is positive.
     */
    public boolean isPositive() {
        return amount.signum() > 0;
    }

    /**
     * Returns true if amount is negative.
     */
    public boolean isNegative() {
        return amount.signum() < 0;
    }

    /**
     * Creates a copy with a new amount.
     * Useful for currency conversion.
     */
    public Money withAmount(BigDecimal newAmount) {
        return new Money(
                newAmount,
                currency
        );
    }

    /**
     * Creates a copy with a new currency.
     * Useful for CurrencyConverter.
     */
    public Money withCurrency(CurrencyType currency) {
        return new Money(
                amount,
                currency
        );
    }

    @Override
    public int compareTo(Money other) {

        validateCurrency(other);

        return amount.compareTo(other.amount);
    }

    private void validateCurrency(Money other) {

        Objects.requireNonNull(
                other,
                "Money cannot be null"
        );

        if (currency != other.currency) {

            throw new InvalidRequestException(
                    String.format(
                            "Currency mismatch. Expected %s but found %s",
                            currency,
                            other.currency
                    )
            );
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Money other)) {
            return false;
        }

        return amount.compareTo(other.amount) == 0
                && currency == other.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                amount.stripTrailingZeros(),
                currency
        );
    }

    @Override
    public String toString() {
        return currency + " " + amount.toPlainString();
    }
}