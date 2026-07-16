package com.billing.pricing.currency;

import com.billing.config.BillingProperties;
import com.billing.domain.common.Money;
import com.billing.domain.enums.CurrencyType;
import com.billing.domain.invoice.Invoice;
import com.billing.exception.ConfigurationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Converts invoice monetary amounts from USD (base) into a requested {@link CurrencyType}. */
public class InvoiceCurrencyConverter {

    private final Map<CurrencyType, BigDecimal> exchangeRates;

    public InvoiceCurrencyConverter(BillingProperties billingProperties) {
        this.exchangeRates = Map.copyOf(
                Objects.requireNonNullElse(billingProperties.getCurrency().getExchangeRates(), Map.of()));
    }

    public Invoice convert(Invoice invoice, CurrencyType targetCurrency) {
        Objects.requireNonNull(invoice, "Invoice cannot be null.");
        Objects.requireNonNull(targetCurrency, "Currency type is required.");

        if (targetCurrency == CurrencyType.USD) {
            return invoice;
        }

        BigDecimal rate = requireRate(targetCurrency);

        List<Invoice.LineItem> lineItems = invoice.lineItems().stream()
                .map(line -> new Invoice.LineItem(
                        line.resourceId(),
                        line.description(),
                        line.quantity(),
                        line.unit(),
                        convertAmount(line.amount(), rate)))
                .toList();

        List<Invoice.ServiceSubtotal> subtotals = invoice.serviceSubtotals().stream()
                .map(subtotal -> new Invoice.ServiceSubtotal(
                        subtotal.serviceType(),
                        convertAmount(subtotal.amount(), rate),
                        subtotal.lineItems().stream()
                                .map(line -> new Invoice.LineItem(
                                        line.resourceId(),
                                        line.description(),
                                        line.quantity(),
                                        line.unit(),
                                        convertAmount(line.amount(), rate)))
                                .toList()))
                .toList();

        return new Invoice(
                invoice.userId(),
                invoice.period(),
                lineItems,
                subtotals,
                convertAmount(invoice.total(), rate));
    }

    private Money convertAmount(Money amountInUsd, BigDecimal rate) {
        return Money.of(amountInUsd.amount().multiply(rate));
    }

    private BigDecimal requireRate(CurrencyType targetCurrency) {
        BigDecimal rate = exchangeRates.get(targetCurrency);
        if (rate == null || rate.signum() <= 0) {
            throw new ConfigurationException(
                    "Exchange rate is not configured for currency: " + targetCurrency);
        }
        return rate;
    }
}
