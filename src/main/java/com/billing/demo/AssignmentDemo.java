package com.billing.demo;

import com.billing.application.query.InvoiceQuery;
import com.billing.domain.common.BillingPeriod;
import com.billing.domain.enums.CurrencyType;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.domain.invoice.Invoice;
import com.billing.domain.usage.UsageEvent;
import com.billing.service.BillingService;
import com.billing.support.BillingContextBuilder;

/**
 * Standalone demo: two users, three services, one invoice (USD 466.00).
 */
public final class AssignmentDemo {

    private static final long PERIOD_START = 1767225600000L;
    private static final long PERIOD_END = 1769904000000L;
    private static final long JAN_10_2026_10_00 = 1768039200000L;
    private static final long JAN_15_2026_10_00 = 1768471200000L;
    private static final long JAN_20_2026_10_00 = 1768903200000L;
    private static final long JAN_25_2026_10_00 = 1769335200000L;

    private AssignmentDemo() {
    }

    public static void main(String[] args) {
        BillingService billingService = BillingContextBuilder.create()
                .withDemoPricing()
                .build()
                .billingService();

        billingService.recordUsage(usage("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "100", JAN_10_2026_10_00));
        billingService.recordUsage(usage("user-1", "cpu-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "150", JAN_15_2026_10_00));
        billingService.recordUsage(usage("user-1", "api-1", ServiceType.API, UnitType.API_CALL, "1400000", JAN_20_2026_10_00));
        billingService.recordUsage(usage("user-2", "disk-2", ServiceType.STORAGE, UnitType.GB_HOUR, "50", JAN_25_2026_10_00));

        BillingPeriod period = new BillingPeriod(PERIOD_START, PERIOD_END);
        Invoice invoice = billingService.generateInvoice(
                InvoiceQuery.of("user-1", period, CurrencyType.USD, null));

        System.out.println("Assignment demo invoice for user-1");
        System.out.println("Period: [" + period.start() + ", " + period.end() + ")");
        System.out.println("Line items: " + invoice.lineItems().size());
        System.out.println("Service subtotals: " + invoice.serviceSubtotals().size());
        System.out.println("Total: " + invoice.total().format(CurrencyType.USD));
        if (!"USD 466.00".equals(invoice.total().format(CurrencyType.USD))) {
            System.err.println("Demo failed: expected total USD 466.00");
            System.exit(1);
        }
    }

    private static UsageEvent usage(
            String userId,
            String resourceId,
            ServiceType serviceType,
            UnitType unit,
            String quantity,
            long timestamp) {
        return UsageEvent.of(userId, resourceId, serviceType, unit, quantity, timestamp);
    }
}
