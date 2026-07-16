package com.billing.service;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.common.Money;
import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.common.UsageQuantity;
import com.billing.domain.enums.CurrencyType;
import com.billing.domain.invoice.Invoice;
import com.billing.domain.usage.UsageEvent;
import com.billing.exception.InvalidRequestException;
import com.billing.storage.UsageRepository;
import com.billing.support.TestEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BillingServiceTest {

    private static final BillingPeriod PERIOD = new BillingPeriod(
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli(),
            Instant.parse("2026-02-01T00:00:00Z").toEpochMilli());

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
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "100", "2026-01-10T10:00:00Z"));
        billingService.recordUsage(TestEvents.compute("user-1", "cpu-1", "150", "2026-01-15T10:00:00Z"));
        billingService.recordUsage(TestEvents.api("user-1", "api-1", "1400000", "2026-01-20T10:00:00Z"));
        billingService.recordUsage(TestEvents.storage("user-2", "disk-2", "50", "2026-01-25T10:00:00Z"));

        Invoice invoice = billingService.generateInvoice("user-1", PERIOD);

        assertNotNull(invoice);
        assertEquals("user-1", invoice.userId());
        assertEquals(3, invoice.serviceSubtotals().size());
        assertEquals(3, invoice.lineItems().size());
        assertEquals("USD 466.00", invoice.total().toString());
    }

    @Test
    void multiResourceFlatLineItemsSumToServiceSubtotal() {
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "60", "2026-01-10T10:00:00Z"));
        billingService.recordUsage(TestEvents.storage("user-1", "disk-2", "40", "2026-01-11T10:00:00Z"));

        Invoice invoice = billingService.generateInvoice("user-1", PERIOD);
        Invoice.ServiceSubtotal storage = subtotalFor(invoice, "storage");
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
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "100", "2026-01-20T10:00:00Z"));
        billingService.recordUsage(TestEvents.compute("user-1", "cpu-1", "150", "2026-01-05T10:00:00Z"));
        billingService.recordUsage(TestEvents.api("user-1", "api-1", "1400000", "2026-01-15T10:00:00Z"));

        Invoice invoice = billingService.generateInvoice("user-1", PERIOD);

        assertEquals("USD 466.00", invoice.total().toString());
        assertEquals("USD 2.00", subtotalFor(invoice, "storage").amount().toString());
        assertEquals("USD 14.00", subtotalFor(invoice, "compute").amount().toString());
        assertEquals("USD 450.00", subtotalFor(invoice, "api").amount().toString());
    }

    @Test
    void billingPeriodIsStartInclusiveAndEndExclusive() {
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "10", "2026-01-01T00:00:00Z"));
        billingService.recordUsage(TestEvents.storage("user-1", "disk-2", "10", "2026-02-01T00:00:00Z"));

        Invoice invoice = billingService.generateInvoice("user-1", PERIOD);

        assertEquals(1, invoice.lineItems().size());
        assertEquals("disk-1", invoice.lineItems().get(0).resourceId());
        assertEquals("USD 0.20", invoice.total().toString());
    }

    @Test
    void rejectsMismatchedUnitTypeForService() {
        UsageEvent invalid = new UsageEvent(
                "user-1", "cpu-1", ServiceKey.of("compute"), UnitKey.of("GB_HOUR"),
                UsageQuantity.of(new BigDecimal("10")), Instant.parse("2026-01-10T10:00:00Z"));

        assertThrows(InvalidRequestException.class, () -> billingService.recordUsage(invalid));
    }

    @Test
    void generatesInvoiceInRequestedCurrency() {
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "50", "2026-01-10T10:00:00Z"));

        Invoice invoice = billingService.generateInvoice("user-1", PERIOD, CurrencyType.EUR);

        assertEquals("EUR 0.92", invoice.total().format(CurrencyType.EUR));
    }

    private static Invoice.ServiceSubtotal subtotalFor(Invoice invoice, String serviceType) {
        ServiceKey key = ServiceKey.of(serviceType);
        return invoice.serviceSubtotals().stream()
                .filter(subtotal -> subtotal.serviceType().equals(key))
                .findFirst()
                .orElseThrow();
    }
}
