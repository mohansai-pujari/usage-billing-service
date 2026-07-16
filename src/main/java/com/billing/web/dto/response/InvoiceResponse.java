package com.billing.web.dto.response;

import com.billing.domain.enums.CurrencyType;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.domain.invoice.Invoice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Invoice for a billing period")
public record InvoiceResponse(
        @Schema(description = "User identifier, or null for an aggregate invoice", example = "user-1") String userId,
        @Schema(description = "Inclusive billing period start (epoch ms)", example = "1767225600000") long billingPeriodStart,
        @Schema(description = "Exclusive billing period end (epoch ms)", example = "1769904000000") long billingPeriodEnd,
        @Schema(example = "USD") CurrencyType currency,
        @Schema(description = "Per-resource line items") List<LineItem> lineItems,
        @Schema(description = "Subtotals grouped by service") List<ServiceSubtotal> serviceSubtotals,
        @Schema(description = "Grand total in the requested currency", example = "USD 466.00") String total
) {
    public static InvoiceResponse from(Invoice invoice, CurrencyType currency) {
        CurrencyType displayCurrency = currency != null ? currency : CurrencyType.USD;
        return new InvoiceResponse(
                invoice.userId(),
                invoice.period().start(),
                invoice.period().end(),
                displayCurrency,
                invoice.lineItems().stream()
                        .map(line -> LineItem.from(line, displayCurrency))
                        .toList(),
                invoice.serviceSubtotals().stream()
                        .map(subtotal -> ServiceSubtotal.from(subtotal, displayCurrency))
                        .toList(),
                invoice.total().format(displayCurrency));
    }

    @Schema(description = "Invoice line item for a single resource")
    public record LineItem(
            String resourceId,
            String description,
            String quantity,
            UnitType unit,
            String amount
    ) {
        static LineItem from(Invoice.LineItem line, CurrencyType currency) {
            return new LineItem(
                    line.resourceId(),
                    line.description(),
                    line.quantity().asBigDecimal().toPlainString(),
                    line.unit(),
                    line.amount().format(currency));
        }
    }

    @Schema(description = "Subtotal for a billable service")
    public record ServiceSubtotal(
            ServiceType serviceType,
            String amount,
            List<LineItem> lineItems
    ) {
        static ServiceSubtotal from(Invoice.ServiceSubtotal subtotal, CurrencyType currency) {
            return new ServiceSubtotal(
                    subtotal.serviceType(),
                    subtotal.amount().format(currency),
                    subtotal.lineItems().stream()
                            .map(line -> LineItem.from(line, currency))
                            .toList());
        }
    }
}
