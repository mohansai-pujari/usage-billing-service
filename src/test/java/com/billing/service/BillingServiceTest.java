package com.billing.service;

import com.billing.application.query.InvoiceQuery;
import com.billing.domain.common.BillingPeriod;
import com.billing.domain.common.Money;
import com.billing.domain.enums.UnitType;
import com.billing.domain.enums.CurrencyType;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.invoice.Invoice;
import com.billing.domain.usage.UsageEvent;
import com.billing.exception.ServiceTypeUnitMismatchException;
import com.billing.storage.UsageRepository;
import com.billing.support.TestEvents;
import com.billing.support.TestTimestamps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BillingServiceTest {

    private static final BillingPeriod PERIOD = new BillingPeriod(
            TestTimestamps.PERIOD_START,
            TestTimestamps.PERIOD_END);

    @Autowired
    private BillingService billingService;

    @Autowired
    private UsageRepository usageRepository;

    @BeforeEach
    void clearStore() {
        usageRepository.clear();
    }

    @Test
    void generatesInvoiceForMultipleServicesAndPricingModels() {
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "100", TestTimestamps.JAN_10_2026_10_00));
        billingService.recordUsage(TestEvents.compute("user-1", "cpu-1", "150", TestTimestamps.JAN_15_2026_10_00));
        billingService.recordUsage(TestEvents.api("user-1", "api-1", "1400000", TestTimestamps.JAN_20_2026_10_00));
        billingService.recordUsage(TestEvents.storage("user-2", "disk-2", "50", TestTimestamps.JAN_25_2026_10_00));

        Invoice invoice = billingService.generateInvoice(InvoiceQuery.of("user-1", PERIOD, CurrencyType.USD, null));

        assertNotNull(invoice);
        assertEquals("user-1", invoice.userId());
        assertEquals(3, invoice.serviceSubtotals().size());
        assertEquals(3, invoice.lineItems().size());
        assertEquals("USD 466.00", invoice.total().toString());
    }

    @Test
    void multiResourceFlatLineItemsSumToServiceSubtotal() {
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "60", TestTimestamps.JAN_10_2026_10_00));
        billingService.recordUsage(TestEvents.storage("user-1", "disk-2", "40", TestTimestamps.JAN_11_2026_10_00));

        Invoice invoice = billingService.generateInvoice(InvoiceQuery.of("user-1", PERIOD, CurrencyType.USD, null));
        Invoice.ServiceSubtotal storage = subtotalFor(invoice, ServiceType.STORAGE);
        Money sumOfLines = storage.lineItems().stream()
                .map(Invoice.LineItem::amount)
                .reduce(Money.zero(), Money::add);

        assertEquals(2, storage.lineItems().size());
        assertEquals("USD 1.20", storage.lineItems().get(0).amount().toString());
        assertEquals("USD 0.80", storage.lineItems().get(1).amount().toString());
        assertEquals("USD 2.00", storage.amount().toString());
        assertEquals(storage.amount(), sumOfLines);
    }

    @Test
    void outOfOrderEventsProduceCorrectTotals() {
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "100", TestTimestamps.JAN_20_2026_10_00));
        billingService.recordUsage(TestEvents.compute("user-1", "cpu-1", "150", TestTimestamps.JAN_05_2026_10_00));
        billingService.recordUsage(TestEvents.api("user-1", "api-1", "1400000", TestTimestamps.JAN_15_2026_10_00));

        Invoice invoice = billingService.generateInvoice(InvoiceQuery.of("user-1", PERIOD, CurrencyType.USD, null));

        assertEquals("USD 466.00", invoice.total().toString());
        assertEquals("USD 2.00", subtotalFor(invoice, ServiceType.STORAGE).amount().toString());
        assertEquals("USD 14.00", subtotalFor(invoice, ServiceType.COMPUTE).amount().toString());
        assertEquals("USD 450.00", subtotalFor(invoice, ServiceType.API).amount().toString());
    }

    @Test
    void billingPeriodIsStartInclusiveAndEndExclusive() {
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "10", TestTimestamps.PERIOD_START));
        billingService.recordUsage(TestEvents.storage("user-1", "disk-2", "10", TestTimestamps.PERIOD_END));

        Invoice invoice = billingService.generateInvoice(InvoiceQuery.of("user-1", PERIOD, CurrencyType.USD, null));

        assertEquals(1, invoice.lineItems().size());
        assertEquals("disk-1", invoice.lineItems().get(0).resourceId());
        assertEquals("USD 0.20", invoice.total().toString());
    }

    @Test
    void rejectsMismatchedUnitTypeForService() {
        UsageEvent invalid = UsageEvent.of(
                "user-1", "cpu-1", ServiceType.COMPUTE, UnitType.GB_HOUR,
                new BigDecimal("10"), TestTimestamps.JAN_10_2026_10_00);

        assertThrows(ServiceTypeUnitMismatchException.class, () -> billingService.recordUsage(invalid));
    }

    @Test
    void generatesInvoiceInRequestedCurrency() {
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "50", TestTimestamps.JAN_10_2026_10_00));

        Invoice invoice = billingService.generateInvoice(InvoiceQuery.of("user-1", PERIOD, CurrencyType.EUR, null));

        assertEquals("EUR 0.92", invoice.total().format(CurrencyType.EUR));
    }

    @Test
    void generatesInvoiceForAllUsersWhenUserIdIsOmitted() {
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "100", TestTimestamps.JAN_10_2026_10_00));
        billingService.recordUsage(TestEvents.storage("user-2", "disk-2", "50", TestTimestamps.JAN_25_2026_10_00));

        Invoice invoice = billingService.generateInvoice(InvoiceQuery.of(null, PERIOD, CurrencyType.USD, null));

        assertEquals(null, invoice.userId());
        assertEquals(2, invoice.lineItems().size());
        assertEquals("USD 3.00", invoice.total().toString());
    }

    @Test
    void generatesInvoiceForSingleServiceWhenServiceTypeIsProvided() {
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "100", TestTimestamps.JAN_10_2026_10_00));
        billingService.recordUsage(TestEvents.compute("user-1", "cpu-1", "150", TestTimestamps.JAN_15_2026_10_00));

        Invoice invoice = billingService.generateInvoice(
                InvoiceQuery.of("user-1", PERIOD, CurrencyType.USD, ServiceType.STORAGE));

        assertEquals(1, invoice.serviceSubtotals().size());
        assertEquals(ServiceType.STORAGE, invoice.serviceSubtotals().get(0).serviceType());
        assertEquals("USD 2.00", invoice.total().toString());
    }

    private static Invoice.ServiceSubtotal subtotalFor(Invoice invoice, ServiceType serviceType) {
        return invoice.serviceSubtotals().stream()
                .filter(subtotal -> subtotal.serviceType() == serviceType)
                .findFirst()
                .orElseThrow();
    }
}
