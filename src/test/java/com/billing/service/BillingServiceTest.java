package com.billing.service;

import com.billing.entity.common.BillingPeriod;
import com.billing.entity.common.ResourceId;
import com.billing.entity.common.UsageQuantity;
import com.billing.entity.common.UserId;
import com.billing.entity.invoice.Invoice;
import com.billing.entity.usage.UsageEvent;
import com.billing.enums.ServiceType;
import com.billing.enums.UnitType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BillingServiceTest {

    @Autowired
    private BillingService billingService;

    @Test
    void generatesInvoiceForMultipleServicesAndPricingModels() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-02-01T00:00:00Z");
        BillingPeriod period = new BillingPeriod(start.toEpochMilli(), end.toEpochMilli());

        billingService.recordUsage(new UsageEvent(
                UserId.of("user-1"),
                ResourceId.of("disk-1"),
                ServiceType.STORAGE,
                UnitType.GB_HOUR,
                UsageQuantity.of(new BigDecimal("100")),
                Instant.parse("2026-01-10T10:00:00Z")
        ));

        billingService.recordUsage(new UsageEvent(
                UserId.of("user-1"),
                ResourceId.of("cpu-1"),
                ServiceType.COMPUTE,
                UnitType.COMPUTE_HOUR,
                UsageQuantity.of(new BigDecimal("150")),
                Instant.parse("2026-01-15T10:00:00Z")
        ));

        billingService.recordUsage(new UsageEvent(
                UserId.of("user-1"),
                ResourceId.of("api-1"),
                ServiceType.API,
                UnitType.API_CALL,
                UsageQuantity.of(new BigDecimal("1400000")),
                Instant.parse("2026-01-20T10:00:00Z")
        ));

        billingService.recordUsage(new UsageEvent(
                UserId.of("user-2"),
                ResourceId.of("disk-2"),
                ServiceType.STORAGE,
                UnitType.GB_HOUR,
                UsageQuantity.of(new BigDecimal("50")),
                Instant.parse("2026-01-25T10:00:00Z")
        ));

        Invoice invoice = billingService.generateInvoice(UserId.of("user-1"), period);

        assertNotNull(invoice);
        assertEquals("user-1", invoice.getUserId().toString());
        assertEquals(3, invoice.getServiceSubtotals().size());
        assertEquals(3, invoice.getLineItems().size());
        assertEquals("USD 66.00", invoice.getTotal().toString());
    }
}
