package com.billing.pricing.currency;

import com.billing.config.BillingProperties;
import com.billing.domain.common.Money;
import com.billing.domain.enums.CurrencyType;
import com.billing.domain.invoice.Invoice;
import com.billing.exception.ConfigurationException;
import com.billing.exception.InvalidRequestException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class InvoiceCurrencyConverter {

    @Autowired
    private BillingProperties billingProperties;

    private Map<CurrencyType, BigDecimal> exchangeRates;

    public InvoiceCurrencyConverter() {
    }

    public InvoiceCurrencyConverter(BillingProperties billingProperties) {
        this.billingProperties = billingProperties;
        this.exchangeRates = loadRates(billingProperties);
    }

    @PostConstruct
    void initialize() {
        this.exchangeRates = loadRates(billingProperties);
    }

    public Invoice convert(Invoice invoice, CurrencyType targetCurrency) {
        Objects.requireNonNull(invoice, "Invoice cannot be null.");
        Objects.requireNonNull(targetCurrency, "Currency type is required.");

        if (!billingProperties.getCurrency().isEnabled() || targetCurrency == CurrencyType.USD) {
            if (!billingProperties.getCurrency().isEnabled() && targetCurrency != CurrencyType.USD) {
                throw new InvalidRequestException("Currency conversion is disabled. Only USD is supported.");
            }
            return invoice;
        }

        BigDecimal rate = requireRate(targetCurrency);
        List<Invoice.LineItem> lineItems = invoice.lineItems().stream()
                .map(line -> convertLine(line, rate))
                .toList();

        List<Invoice.ServiceSubtotal> subtotals = invoice.serviceSubtotals().stream()
                .map(subtotal -> new Invoice.ServiceSubtotal(
                        subtotal.serviceType(),
                        convertAmount(subtotal.amount(), rate),
                        subtotal.lineItems().stream().map(line -> convertLine(line, rate)).toList()))
                .toList();

        return new Invoice(
                invoice.userId(),
                invoice.period(),
                lineItems,
                subtotals,
                convertAmount(invoice.total(), rate));
    }

    private static Invoice.LineItem convertLine(Invoice.LineItem line, BigDecimal rate) {
        return new Invoice.LineItem(
                line.resourceId(),
                line.description(),
                line.quantity(),
                line.unit(),
                convertAmount(line.amount(), rate));
    }

    private static Money convertAmount(Money amountInUsd, BigDecimal rate) {
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

    private static Map<CurrencyType, BigDecimal> loadRates(BillingProperties billingProperties) {
        return Map.copyOf(
                Objects.requireNonNullElse(billingProperties.getCurrency().getExchangeRates(), Map.of()));
    }
}
