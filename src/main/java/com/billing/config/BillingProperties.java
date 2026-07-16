package com.billing.config;

import com.billing.domain.enums.CurrencyType;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "billing")
@Validated
public class BillingProperties {

    @NotEmpty
    private Map<ServiceType, PricingDefinition> pricing = new EnumMap<>(ServiceType.class);
    private CurrencySettings currency = new CurrencySettings();
    private TestSettings test = new TestSettings();

    public Map<ServiceType, PricingDefinition> getPricing() {
        return pricing;
    }

    public void setPricing(Map<ServiceType, PricingDefinition> pricing) {
        this.pricing = pricing;
    }

    public CurrencySettings getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencySettings currency) {
        this.currency = currency;
    }

    public TestSettings getTest() {
        return test;
    }

    public void setTest(TestSettings test) {
        this.test = test;
    }

    public static class TestSettings {

        private BulkUploadSettings bulkUpload = new BulkUploadSettings();

        public BulkUploadSettings getBulkUpload() {
            return bulkUpload;
        }

        public void setBulkUpload(BulkUploadSettings bulkUpload) {
            this.bulkUpload = bulkUpload;
        }
    }

    public static class BulkUploadSettings {

        private List<String> allowedEnvironments = new ArrayList<>(List.of("local", "dev"));

        public List<String> getAllowedEnvironments() {
            return allowedEnvironments;
        }

        public void setAllowedEnvironments(List<String> allowedEnvironments) {
            this.allowedEnvironments = allowedEnvironments;
        }
    }

    public static class CurrencySettings {

        private boolean enabled = true;
        private Map<CurrencyType, BigDecimal> exchangeRates = new EnumMap<>(CurrencyType.class);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Map<CurrencyType, BigDecimal> getExchangeRates() {
            return exchangeRates;
        }

        public void setExchangeRates(Map<CurrencyType, BigDecimal> exchangeRates) {
            this.exchangeRates = exchangeRates;
        }
    }

    public static class PricingDefinition {

        private String billingType;
        private UnitType unit;
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

        public UnitType getUnit() {
            return unit;
        }

        public void setUnit(UnitType unit) {
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
