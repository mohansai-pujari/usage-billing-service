package com.billing.service;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.common.Money;
import com.billing.domain.common.ServiceKey;
import com.billing.domain.invoice.Invoice;
import com.billing.storage.UsageRepository;
import com.billing.support.TestEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validates the exact pricing scenarios documented in {@code REQUIREMENTS.md}.
 */
@SpringBootTest
class AssignmentScenarioTest {

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
    void flatPricingExampleFromAssignment() {
        // Storage: 50 GB-hours × $0.02 = $1.00
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "50", "2026-01-10T10:00:00Z"));

        Invoice invoice = billingService.generateInvoice("user-1", PERIOD);

        assertEquals("USD 1.00", invoice.total().toString());
        assertEquals(new BigDecimal("50"), lineQuantity(invoice, "disk-1"));
    }

    @Test
    void tieredPricingExampleFromAssignment() {
        // Compute: 150 hours → 100 × 0.10 + 50 × 0.08 = $14.00
        billingService.recordUsage(TestEvents.compute("user-1", "cpu-1", "150", "2026-01-10T10:00:00Z"));

        Invoice invoice = billingService.generateInvoice("user-1", PERIOD);

        assertEquals("USD 14.00", subtotal(invoice, "compute").toString());
    }

    @Test
    void subscriptionPricingExampleFromAssignment() {
        // API: 1,400,000 calls → $50 + 400,000 × 0.001 = $450.00
        billingService.recordUsage(TestEvents.api("user-1", "api-1", "1400000", "2026-01-10T10:00:00Z"));

        Invoice invoice = billingService.generateInvoice("user-1", PERIOD);

        assertEquals("USD 450.00", subtotal(invoice, "api").toString());
    }

    @Test
    void assignmentDeliverableScenarioWithTwoUsersAndThreeServices() {
        billingService.recordUsage(TestEvents.storage("user-1", "disk-1", "100", "2026-01-10T10:00:00Z"));
        billingService.recordUsage(TestEvents.compute("user-1", "cpu-1", "150", "2026-01-15T10:00:00Z"));
        billingService.recordUsage(TestEvents.api("user-1", "api-1", "1400000", "2026-01-20T10:00:00Z"));
        billingService.recordUsage(TestEvents.storage("user-2", "disk-2", "50", "2026-01-25T10:00:00Z"));

        Invoice invoice = billingService.generateInvoice("user-1", PERIOD);

        assertEquals(3, invoice.serviceSubtotals().size());
        assertEquals("USD 2.00", subtotal(invoice, "storage").toString());
        assertEquals("USD 14.00", subtotal(invoice, "compute").toString());
        assertEquals("USD 450.00", subtotal(invoice, "api").toString());
        assertEquals("USD 466.00", invoice.total().toString());
    }

    private static Money subtotal(Invoice invoice, String serviceType) {
        ServiceKey key = ServiceKey.of(serviceType);
        return invoice.serviceSubtotals().stream()
                .filter(item -> item.serviceType().equals(key))
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
