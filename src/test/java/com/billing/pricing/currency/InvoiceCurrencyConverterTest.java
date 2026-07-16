package com.billing.pricing.currency;

import com.billing.config.BillingProperties;
import com.billing.domain.common.BillingPeriod;
import com.billing.domain.common.Money;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.domain.common.UsageQuantity;
import com.billing.domain.enums.CurrencyType;
import com.billing.domain.invoice.Invoice;
import com.billing.exception.ConfigurationException;
import com.billing.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceCurrencyConverterTest {

    private InvoiceCurrencyConverter converter;

    @BeforeEach
    void setUp() {
        BillingProperties properties = new BillingProperties();
        Map<CurrencyType, BigDecimal> rates = new EnumMap<>(CurrencyType.class);
        rates.put(CurrencyType.EUR, new BigDecimal("0.92"));
        rates.put(CurrencyType.INR, new BigDecimal("83.12"));
        properties.getCurrency().setEnabled(true);
        properties.getCurrency().setExchangeRates(rates);
        converter = new InvoiceCurrencyConverter(properties);
    }

    @Test
    void leavesUsdInvoiceUnchanged() {
        Invoice invoice = sampleInvoice(Money.of(new BigDecimal("10.00")));

        Invoice converted = converter.convert(invoice, CurrencyType.USD);

        assertEquals("USD 10.00", converted.total().format(CurrencyType.USD));
        assertEquals("USD 10.00", converted.lineItems().get(0).amount().format(CurrencyType.USD));
    }

    @Test
    void convertsAllInvoiceAmountsToTargetCurrency() {
        Invoice invoice = sampleInvoice(Money.of(new BigDecimal("10.00")));

        Invoice converted = converter.convert(invoice, CurrencyType.EUR);

        assertEquals("EUR 9.20", converted.total().format(CurrencyType.EUR));
        assertEquals("EUR 9.20", converted.lineItems().get(0).amount().format(CurrencyType.EUR));
        assertEquals("EUR 9.20", converted.serviceSubtotals().get(0).amount().format(CurrencyType.EUR));
        assertEquals(UnitType.GB_HOUR, converted.lineItems().get(0).unit());
    }

    @Test
    void rejectsUnsupportedCurrencyWithoutRate() {
        Invoice invoice = sampleInvoice(Money.of(new BigDecimal("10.00")));

        assertThrows(ConfigurationException.class, () -> converter.convert(invoice, CurrencyType.GBP));
    }

    @Test
    void rejectsNonUsdWhenConversionDisabled() {
        BillingProperties properties = new BillingProperties();
        properties.getCurrency().setEnabled(false);
        InvoiceCurrencyConverter disabledConverter = new InvoiceCurrencyConverter(properties);
        Invoice invoice = sampleInvoice(Money.of(new BigDecimal("10.00")));

        assertThrows(InvalidRequestException.class,
                () -> disabledConverter.convert(invoice, CurrencyType.EUR));
    }

    private static Invoice sampleInvoice(Money amount) {
        BillingPeriod period = new BillingPeriod(1L, 2L);
        Invoice.LineItem lineItem = new Invoice.LineItem(
                "disk-1",
                "storage usage",
                UsageQuantity.of(new BigDecimal("50")),
                UnitType.GB_HOUR,
                amount);
        Invoice.ServiceSubtotal subtotal = new Invoice.ServiceSubtotal(
                ServiceType.STORAGE,
                amount,
                List.of(lineItem));
        return new Invoice("user-1", period, List.of(lineItem), List.of(subtotal), amount);
    }
}
