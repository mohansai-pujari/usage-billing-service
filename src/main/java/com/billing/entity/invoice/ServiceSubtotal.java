package com.billing.entity.invoice;

import com.billing.entity.common.Money;
import com.billing.enums.ServiceType;

import java.util.List;
import java.util.Objects;

public class ServiceSubtotal {

    private final ServiceType serviceType;
    private final Money subtotal;
    private final List<InvoiceLineItem> lineItems;

    public ServiceSubtotal(ServiceType serviceType, Money subtotal, List<InvoiceLineItem> lineItems) {
        this.serviceType = Objects.requireNonNull(serviceType, "Service type cannot be null.");
        this.subtotal = Objects.requireNonNull(subtotal, "Subtotal cannot be null.");
        this.lineItems = List.copyOf(lineItems);
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public Money getSubtotal() {
        return subtotal;
    }

    public List<InvoiceLineItem> getLineItems() {
        return lineItems;
    }
}
