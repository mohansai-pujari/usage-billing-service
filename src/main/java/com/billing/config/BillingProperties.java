package com.billing.config;

import com.billing.domain.enums.CurrencyType;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Root configuration properties bound from {@code billing.*} in {@code application.yml}.
 * <p>
 * Responsibility: Holds per-service pricing definitions loaded at startup. Consumed by
 * {@link com.billing.pricing.registry.PricingConfigurationRegistry} to build runtime {@link com.billing.domain.pricing.PricingConfig} objects.
 * <p>
 * Design patterns: Externalized Configuration, Data Transfer Object (YAML binding target).
 */
@ConfigurationProperties(prefix = "billing")
@Validated
public class BillingProperties {

    @NotEmpty
    private Map<String, PricingDefinition> pricing = new LinkedHashMap<>();
    private CurrencySettings currency = new CurrencySettings();

    /** Returns the map of service type to pricing definition from YAML. */
    public Map<String, PricingDefinition> getPricing() {
        return pricing;
    }

    /** Sets the pricing map (used by Spring Boot property binding). */
    public void setPricing(Map<String, PricingDefinition> pricing) {
        this.pricing = pricing;
    }

    public CurrencySettings getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencySettings currency) {
        this.currency = currency;
    }

    /** YAML binding model for invoice currency exchange rates (base currency is USD). */
    public static class CurrencySettings {

        private Map<CurrencyType, BigDecimal> exchangeRates = new EnumMap<>(CurrencyType.class);

        public Map<CurrencyType, BigDecimal> getExchangeRates() {
            return exchangeRates;
        }

        public void setExchangeRates(Map<CurrencyType, BigDecimal> exchangeRates) {
            this.exchangeRates = exchangeRates;
        }
    }

    /**
     * YAML binding model for a single service's pricing rules.
     * Fields used depend on billing type (flat, tiered, or subscription).
     */
    public static class PricingDefinition {

        private String billingType;
        private String unit;
        private BigDecimal unitPrice;
        private List<TierDefinition> tiers = new ArrayList<>();
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

        public List<TierDefinition> getTiers() {
            return tiers;
        }

        public void setTiers(List<TierDefinition> tiers) {
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

    /** YAML binding model for one tier in tiered pricing. */
    public static class TierDefinition {

        private Long upTo;
        private BigDecimal unitPrice;

        public Long getUpTo() {
            return upTo;
        }

        public void setUpTo(Long upTo) {
            this.upTo = upTo;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}
