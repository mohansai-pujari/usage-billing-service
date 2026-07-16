package com.billing.entity.invoice;

import com.billing.entity.common.BillingPeriod;
import com.billing.entity.common.UserId;

import java.util.List;
import java.util.Objects;

public class Invoice {

    private final UserId userId;
    private final BillingPeriod billingPeriod;
    private final List<InvoiceLineItem> lineItems;
    private final List<ServiceSubtotal> serviceSubtotals;
    private final com.billing.entity.common.Money total;

    public Invoice(UserId userId, BillingPeriod billingPeriod, List<InvoiceLineItem> lineItems,
                   List<ServiceSubtotal> serviceSubtotals, com.billing.entity.common.Money total) {

        this.userId = Objects.requireNonNull(userId, "UserId cannot be null.");
        this.billingPeriod = Objects.requireNonNull(billingPeriod, "Billing period cannot be null.");
        this.lineItems = List.copyOf(lineItems);
        this.serviceSubtotals = List.copyOf(serviceSubtotals);
        this.total = Objects.requireNonNull(total, "Total cannot be null.");
    }

    public UserId getUserId() {
        return userId;
    }

    public BillingPeriod getBillingPeriod() {
        return billingPeriod;
    }

    public List<InvoiceLineItem> getLineItems() {
        return lineItems;
    }

    public List<ServiceSubtotal> getServiceSubtotals() {
        return serviceSubtotals;
    }

    public com.billing.entity.common.Money getTotal() {
        return total;
    }
}
