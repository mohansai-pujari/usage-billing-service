package com.billing.web.dto.response;

import com.billing.domain.enums.CurrencyType;
import com.billing.domain.invoice.Invoice;

import java.util.List;

/** Outgoing invoice payload for {@code GET /invoices/{userId}}. */
public record InvoiceResponse(
        String userId,
        long billingPeriodStart,
        long billingPeriodEnd,
        CurrencyType currency,
        List<InvoiceLineItemResponse> lineItems,
        List<InvoiceServiceSubtotalResponse> serviceSubtotals,
        String total
) {
    public static InvoiceResponse from(Invoice invoice, CurrencyType currency) {
        CurrencyType displayCurrency = currency != null ? currency : CurrencyType.USD;
        return new InvoiceResponse(
                invoice.userId(),
                invoice.period().start(),
                invoice.period().end(),
                displayCurrency,
                invoice.lineItems().stream()
                        .map(line -> InvoiceLineItemResponse.from(line, displayCurrency))
                        .toList(),
                invoice.serviceSubtotals().stream()
                        .map(subtotal -> InvoiceServiceSubtotalResponse.from(subtotal, displayCurrency))
                        .toList(),
                invoice.total().format(displayCurrency));
    }
}
