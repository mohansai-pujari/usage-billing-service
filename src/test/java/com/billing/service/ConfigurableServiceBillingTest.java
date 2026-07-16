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

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies a service added only via configuration can be billed end-to-end without code changes.
 */
@SpringBootTest(properties = {
        "billing.pricing.archive.billing-type=FLAT",
        "billing.pricing.archive.unit=GB_HOUR",
        "billing.pricing.archive.unit-price=0.03"
})
class ConfigurableServiceBillingTest {

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
    void billsYamlOnlyServiceWithoutCodeChanges() {
        billingService.recordUsage(
                TestEvents.event("user-1", "archive-1", "archive", "GB_HOUR", "100", "2026-01-10T10:00:00Z"));

        Invoice invoice = billingService.generateInvoice("user-1", PERIOD);

        assertEquals("USD 3.00", subtotal(invoice, "archive").toString());
        assertEquals("USD 3.00", invoice.total().toString());
    }

    private static Money subtotal(Invoice invoice, String serviceType) {
        ServiceKey key = ServiceKey.of(serviceType);
        return invoice.serviceSubtotals().stream()
                .filter(item -> item.serviceType().equals(key))
                .findFirst()
                .orElseThrow()
                .amount();
    }
}
