package com.billing.support;

import com.billing.service.BillingService;
import com.billing.storage.UsageRepository;

public record BillingContext(
        UsageRepository usageRepository,
        BillingService billingService) {
}
