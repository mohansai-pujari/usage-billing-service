package com.billing.pricing.strategy;

import com.billing.entity.common.Money;
import com.billing.entity.pricing.PricingConfig;
import com.billing.entity.usage.ServiceUsageSummary;
import com.billing.enums.BillingType;

public interface PricingStrategy<T extends PricingConfig> {

    BillingType supportedBillingType();

    Money calculate(ServiceUsageSummary usage, T pricingConfig);
}