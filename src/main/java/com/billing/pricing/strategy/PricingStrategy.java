package com.billing.pricing.strategy;

import com.billing.config.BillingProperties.PricingDefinition;
import com.billing.domain.common.Money;
import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.enums.BillingType;
import com.billing.domain.pricing.PricingConfig;
import com.billing.domain.usage.ServiceUsageSummary;

import java.math.BigDecimal;
import java.util.List;

/** Strategy contract for a billing-type-specific pricing model. */
public interface PricingStrategy {

    BillingType billingType();

    PricingConfig buildConfig(ServiceKey serviceType, UnitKey unitType, PricingDefinition definition);

    Money calculate(PricingConfig config, BigDecimal quantity);

    List<Money> allocateResourceLineAmounts(
            PricingConfig config,
            ServiceUsageSummary usage,
            Money serviceCharge);
}
