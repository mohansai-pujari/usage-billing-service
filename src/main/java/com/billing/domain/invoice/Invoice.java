package com.billing.domain.invoice;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.common.Money;
import com.billing.domain.common.UsageQuantity;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;

import java.util.List;

public record Invoice(
        String userId,
        BillingPeriod period,
        List<LineItem> lineItems,
        List<ServiceSubtotal> serviceSubtotals,
        Money total
) {
    public record LineItem(
            String resourceId,
            String description,
            UsageQuantity quantity,
            UnitType unit,
            Money amount
    ) {
    }

    public record ServiceSubtotal(
            ServiceType serviceType,
            Money amount,
            List<LineItem> lineItems
    ) {
    }
}
