package com.billing.domain.invoice;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.common.Money;
import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.common.UsageQuantity;

import java.util.List;

/** Complete invoice for a user over a billing period. */
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
            UnitKey unit,
            Money amount
    ) {
    }

    public record ServiceSubtotal(
            ServiceKey serviceType,
            Money amount,
            List<LineItem> lineItems
    ) {
    }
}
