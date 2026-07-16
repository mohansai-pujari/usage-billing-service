package com.billing.dto.response;

import com.billing.entity.invoice.Invoice;
import com.billing.entity.invoice.InvoiceLineItem;
import com.billing.entity.invoice.ServiceSubtotal;

import java.util.List;

public record InvoiceResponse(
        String userId,
        long billingPeriodStart,
        long billingPeriodEnd,
        List<InvoiceLineItem> lineItems,
        List<ServiceSubtotal> serviceSubtotals,
        String total
) {
    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getUserId().toString(),
                invoice.getBillingPeriod().getStart(),
                invoice.getBillingPeriod().getEnd(),
                invoice.getLineItems(),
                invoice.getServiceSubtotals(),
                invoice.getTotal().toString()
        );
    }
}
