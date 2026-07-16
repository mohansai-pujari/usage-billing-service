package com.billing.application.query;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.common.ValidationRules;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.usage.UsageEvent;
import com.billing.exception.InvalidRequestException;

public record UsageQuery(String userId, ServiceType serviceType, BillingPeriod period) {

    public UsageQuery {
        if (period == null) {
            throw new InvalidRequestException("Billing period cannot be null.");
        }
    }

    public static UsageQuery of(String userId, ServiceType serviceType, BillingPeriod period) {
        return new UsageQuery(ValidationRules.optionalTrimmed(userId), serviceType, period);
    }

    public boolean matches(UsageEvent event) {
        if (serviceType != null && event.serviceType() != serviceType) {
            return false;
        }
        return period.contains(event.timestamp());
    }
}
