package com.billing.web.dto.response;

import com.billing.domain.enums.CurrencyType;
import com.billing.domain.invoice.Invoice;

/** API view of a single invoice line item. */
public record InvoiceLineItemResponse(
        String resourceId,
        String description,
        String quantity,
        String unit,
        String amount
) {
    static InvoiceLineItemResponse from(Invoice.LineItem line, CurrencyType currency) {
        return new InvoiceLineItemResponse(
                line.resourceId(),
                line.description(),
                line.quantity().asBigDecimal().toPlainString(),
                line.unit().value(),
                line.amount().format(currency));
    }
}
