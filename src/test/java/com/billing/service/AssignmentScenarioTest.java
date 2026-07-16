package com.billing.service;

import com.billing.application.query.InvoiceQuery;
import com.billing.domain.common.BillingPeriod;
import com.billing.domain.common.Money;
import com.billing.domain.enums.CurrencyType;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.invoice.Invoice;
import com.billing.storage.UsageRepository;
import com.billing.support.TestEvents;
import com.billing.support.TestTimestamps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validates the exact pricing scenarios documented in {@code REQUIREMENTS.md}.
 */
@SpringBootTest
class AssignmentScenarioTest {

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
    void flatPricingExampleFromAssignment() {
        // Storage: 50 GB-hours × $0.02 = $1.00
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "50", TestTimestamps.JAN_10_2026_10_00));

        Invoice invoice = billingService.generateInvoice(InvoiceQuery.of("user-1", PERIOD, CurrencyType.USD, null));

        assertEquals("USD 1.00", invoice.total().toString());
        assertEquals(0, new BigDecimal("50").compareTo(lineQuantity(invoice, "disk-1")));
    }

    @Test
    void tieredPricingExampleFromAssignment() {
        // Compute: 150 hours → 100 × 0.10 + 50 × 0.08 = $14.00
        billingService.recordUsage(TestEvents.compute("user-1", "cpu-1", "150", TestTimestamps.JAN_10_2026_10_00));

        Invoice invoice = billingService.generateInvoice(InvoiceQuery.of("user-1", PERIOD, CurrencyType.USD, null));

        assertEquals("USD 14.00", subtotal(invoice, ServiceType.COMPUTE).toString());
    }

    @Test
    void subscriptionPricingExampleFromAssignment() {
        // API: 1,400,000 calls → $50 + 400,000 × 0.001 = $450.00
        billingService.recordUsage(TestEvents.api("user-1", "api-1", "1400000", TestTimestamps.JAN_10_2026_10_00));

        Invoice invoice = billingService.generateInvoice(InvoiceQuery.of("user-1", PERIOD, CurrencyType.USD, null));

        assertEquals("USD 450.00", subtotal(invoice, ServiceType.API).toString());
    }

    @Test
    void assignmentDeliverableScenarioWithTwoUsersAndThreeServices() {
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "100", TestTimestamps.JAN_10_2026_10_00));
        billingService.recordUsage(TestEvents.compute("user-1", "cpu-1", "150", TestTimestamps.JAN_15_2026_10_00));
        billingService.recordUsage(TestEvents.api("user-1", "api-1", "1400000", TestTimestamps.JAN_20_2026_10_00));
        billingService.recordUsage(TestEvents.storage("user-2", "disk-2", "50", TestTimestamps.JAN_25_2026_10_00));

        Invoice invoice = billingService.generateInvoice(InvoiceQuery.of("user-1", PERIOD, CurrencyType.USD, null));

        assertEquals(3, invoice.serviceSubtotals().size());
        assertEquals("USD 2.00", subtotal(invoice, ServiceType.STORAGE).toString());
        assertEquals("USD 14.00", subtotal(invoice, ServiceType.COMPUTE).toString());
        assertEquals("USD 450.00", subtotal(invoice, ServiceType.API).toString());
        assertEquals("USD 466.00", invoice.total().toString());
    }

    private static Money subtotal(Invoice invoice, ServiceType serviceType) {
        return invoice.serviceSubtotals().stream()
                .filter(item -> item.serviceType() == serviceType)
                .findFirst()
                .orElseThrow()
                .amount();
    }

    private static BigDecimal lineQuantity(Invoice invoice, String resourceId) {
        return invoice.lineItems().stream()
                .filter(line -> line.resourceId().equals(resourceId))
                .findFirst()
                .orElseThrow()
                .quantity()
                .asBigDecimal();
    }
}
