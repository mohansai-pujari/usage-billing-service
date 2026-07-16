package com.billing.pricing.strategy;

import com.billing.config.BillingProperties.PricingDefinition;
import com.billing.domain.common.Money;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.domain.enums.BillingType;
import com.billing.domain.pricing.PricingConfig;
import com.billing.domain.usage.ServiceUsageSummary;

import java.math.BigDecimal;
import java.util.List;

/** Strategy contract for a billing-type-specific pricing model. */
public interface PricingStrategy {

    BillingType billingType();

    PricingConfig buildConfig(ServiceType serviceType, UnitType unitType, PricingDefinition definition);

    Money calculate(PricingConfig config, BigDecimal quantity);

    List<Money> allocateResourceLineAmounts(
            PricingConfig config,
            ServiceUsageSummary usage,
            Money serviceCharge);
}
