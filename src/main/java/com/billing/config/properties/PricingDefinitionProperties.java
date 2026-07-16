package com.billing.config.properties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic pricing definition loaded from YAML.
 *
 * Depending on billingType,
 * only a subset of fields will be populated.
 */
public class PricingDefinitionProperties {

    private String billingType;

    private String unit;

    // Flat

    private BigDecimal unitPrice;

    // Tiered

    private List<TierProperties> tiers =
            new ArrayList<>();

    // Subscription

    private BigDecimal monthlyFee;

    private Long includedUnits;

    private BigDecimal overageUnitPrice;

    public String getBillingType() {
        return billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public List<TierProperties> getTiers() {
        return tiers;
    }

    public void setTiers(List<TierProperties> tiers) {
        this.tiers = tiers;
    }

    public BigDecimal getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(BigDecimal monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public Long getIncludedUnits() {
        return includedUnits;
    }

    public void setIncludedUnits(Long includedUnits) {
        this.includedUnits = includedUnits;
    }

    public BigDecimal getOverageUnitPrice() {
        return overageUnitPrice;
    }

    public void setOverageUnitPrice(BigDecimal overageUnitPrice) {
        this.overageUnitPrice = overageUnitPrice;
    }
}