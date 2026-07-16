package com.billing.web.dto.response;

import com.billing.domain.enums.CurrencyType;
import com.billing.domain.invoice.Invoice;

import java.util.List;

/** API view of a per-service invoice subtotal. */
public record InvoiceServiceSubtotalResponse(
        String serviceType,
        String amount,
        List<InvoiceLineItemResponse> lineItems
) {
    static InvoiceServiceSubtotalResponse from(Invoice.ServiceSubtotal subtotal, CurrencyType currency) {
        return new InvoiceServiceSubtotalResponse(
                subtotal.serviceType().value(),
                subtotal.amount().format(currency),
                subtotal.lineItems().stream()
                        .map(line -> InvoiceLineItemResponse.from(line, currency))
                        .toList());
    }
}
