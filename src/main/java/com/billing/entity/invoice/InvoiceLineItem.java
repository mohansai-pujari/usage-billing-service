package com.billing.entity.invoice;

import com.billing.entity.common.Money;
import com.billing.enums.UnitType;

import java.math.BigDecimal;
import java.util.Objects;

public class InvoiceLineItem {

    private final String resourceId;
    private final String description;
    private final BigDecimal quantity;
    private final UnitType unitType;
    private final Money amount;

    public InvoiceLineItem(String resourceId, String description, BigDecimal quantity, UnitType unitType, Money amount) {
        this.resourceId = Objects.requireNonNull(resourceId, "Resource ID cannot be null.");
        this.description = Objects.requireNonNull(description, "Description cannot be null.");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null.");
        this.unitType = Objects.requireNonNull(unitType, "Unit type cannot be null.");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null.");
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public Money getAmount() {
        return amount;
    }
}
