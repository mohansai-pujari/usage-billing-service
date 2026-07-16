package com.billing.application.query;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.common.ValidationRules;
import com.billing.domain.enums.CurrencyType;
import com.billing.domain.enums.ServiceType;
import com.billing.exception.InvalidRequestException;

public record InvoiceQuery(
        String userId,
        BillingPeriod period,
        CurrencyType currency,
        ServiceType serviceType) {

    public InvoiceQuery {
        if (period == null) {
            throw new InvalidRequestException("Billing period cannot be null.");
        }
        if (currency == null) {
            throw new InvalidRequestException("Currency type is required.");
        }
    }

    public static InvoiceQuery of(
            String userId,
            BillingPeriod period,
            CurrencyType currency,
            ServiceType serviceType) {
        return new InvoiceQuery(
                ValidationRules.optionalTrimmed(userId),
                period,
                currency != null ? currency : CurrencyType.USD,
                serviceType);
    }

    public UsageQuery toUsageQuery() {
        return UsageQuery.of(userId, serviceType, period);
    }
}
