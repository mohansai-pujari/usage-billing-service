package com.billing.service;

import com.billing.entity.common.BillingPeriod;
import com.billing.entity.common.UserId;
import com.billing.entity.invoice.Invoice;
import com.billing.entity.usage.UsageEvent;

public interface BillingService {

    void recordUsage(UsageEvent usageEvent);

    Invoice generateInvoice(UserId userId, BillingPeriod billingPeriod);
}
