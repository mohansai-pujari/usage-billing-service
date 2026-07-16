package com.billing.support;

import com.billing.config.BillingProperties;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;

import java.math.BigDecimal;
import java.util.List;

public final class DemoPricingProperties {

    private DemoPricingProperties() {
    }

    public static BillingProperties create() {
        BillingProperties properties = new BillingProperties();

        BillingProperties.PricingDefinition storage = new BillingProperties.PricingDefinition();
        storage.setBillingType("FLAT");
        storage.setUnit(UnitType.GB_HOUR);
        storage.setUnitPrice(new BigDecimal("0.02"));

        BillingProperties.PricingDefinition compute = new BillingProperties.PricingDefinition();
        compute.setBillingType("TIERED");
        compute.setUnit(UnitType.COMPUTE_HOUR);
        BillingProperties.TierDefinition tier1 = new BillingProperties.TierDefinition();
        tier1.setUpTo(100L);
        tier1.setUnitPrice(new BigDecimal("0.10"));
        BillingProperties.TierDefinition tier2 = new BillingProperties.TierDefinition();
        tier2.setUpTo(1000L);
        tier2.setUnitPrice(new BigDecimal("0.08"));
        BillingProperties.TierDefinition tier3 = new BillingProperties.TierDefinition();
        tier3.setUnitPrice(new BigDecimal("0.05"));
        compute.setTiers(List.of(tier1, tier2, tier3));

        BillingProperties.PricingDefinition api = new BillingProperties.PricingDefinition();
        api.setBillingType("SUBSCRIPTION");
        api.setUnit(UnitType.API_CALL);
        api.setMonthlyFee(new BigDecimal("50"));
        api.setIncludedUnits(1_000_000L);
        api.setOverageUnitPrice(new BigDecimal("0.001"));

        properties.getPricing().put(ServiceType.STORAGE, storage);
        properties.getPricing().put(ServiceType.COMPUTE, compute);
        properties.getPricing().put(ServiceType.API, api);
        return properties;
    }
}
