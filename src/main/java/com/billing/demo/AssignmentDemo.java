package com.billing.demo;

import com.billing.config.BillingProperties;
import com.billing.domain.common.BillingPeriod;
import com.billing.domain.enums.CurrencyType;
import com.billing.domain.invoice.Invoice;
import com.billing.pricing.BillingCalculator;
import com.billing.pricing.InvoiceAssembler;
import com.billing.pricing.UsageAggregator;
import com.billing.pricing.currency.InvoiceCurrencyConverter;
import com.billing.pricing.registry.PricingConfigurationRegistry;
import com.billing.pricing.strategy.impl.FlatPricingStrategy;
import com.billing.pricing.strategy.impl.SubscriptionPricingStrategy;
import com.billing.pricing.strategy.impl.TieredPricingStrategy;
import com.billing.pricing.strategy.registry.PricingStrategyRegistry;
import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.common.UsageQuantity;
import com.billing.domain.usage.UsageEvent;
import com.billing.service.BillingService;
import com.billing.storage.UsageStore;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Standalone driver demonstrating the assignment deliverable without HTTP.
 * Two users, three services, all pricing models, one generated invoice.
 */
public final class AssignmentDemo {

    private AssignmentDemo() {
    }

    public static void main(String[] args) {
        BillingProperties properties = demoProperties();
        PricingStrategyRegistry strategyRegistry = new PricingStrategyRegistry(List.of(
                new FlatPricingStrategy(),
                new TieredPricingStrategy(),
                new SubscriptionPricingStrategy()));
        PricingConfigurationRegistry pricingRegistry = new PricingConfigurationRegistry(properties, strategyRegistry);
        UsageStore usageStore = new UsageStore();
        InvoiceAssembler invoiceAssembler = new InvoiceAssembler(
                new UsageAggregator(),
                new BillingCalculator(strategyRegistry),
                pricingRegistry);
        BillingService billingService = new BillingService(
                usageStore,
                invoiceAssembler,
                pricingRegistry,
                new InvoiceCurrencyConverter(properties));

        billingService.recordUsage(usage("user-1", "disk-1", "storage", "GB_HOUR", "100", "2026-01-10T10:00:00Z"));
        billingService.recordUsage(usage("user-1", "cpu-1", "compute", "COMPUTE_HOUR", "150", "2026-01-15T10:00:00Z"));
        billingService.recordUsage(usage("user-1", "api-1", "api", "API_CALL", "1400000", "2026-01-20T10:00:00Z"));
        billingService.recordUsage(usage("user-2", "disk-2", "storage", "GB_HOUR", "50", "2026-01-25T10:00:00Z"));

        BillingPeriod period = new BillingPeriod(
                Instant.parse("2026-01-01T00:00:00Z").toEpochMilli(),
                Instant.parse("2026-02-01T00:00:00Z").toEpochMilli());
        Invoice invoice = billingService.generateInvoice("user-1", period, CurrencyType.USD);

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

    private static BillingProperties demoProperties() {
        BillingProperties properties = new BillingProperties();

        BillingProperties.PricingDefinition storage = new BillingProperties.PricingDefinition();
        storage.setBillingType("FLAT");
        storage.setUnit("GB_HOUR");
        storage.setUnitPrice(new BigDecimal("0.02"));

        BillingProperties.PricingDefinition compute = new BillingProperties.PricingDefinition();
        compute.setBillingType("TIERED");
        compute.setUnit("COMPUTE_HOUR");
        BillingProperties.TierDefinition tier1 = new BillingProperties.TierDefinition();
        tier1.setUpTo(100L);
        tier1.setUnitPrice(new BigDecimal("0.10"));
        BillingProperties.TierDefinition tier2 = new BillingProperties.TierDefinition();
        tier2.setUpTo(1000L);
        tier2.setUnitPrice(new BigDecimal("0.08"));
        BillingProperties.TierDefinition tier3 = new BillingProperties.TierDefinition();
        tier3.setUnitPrice(new BigDecimal("0.05"));
        compute.setTiers(List.of(tier1, tier2, tier3));

        BillingProperties.PricingDefinition api = new BillingProperties.PricingDefinition();
        api.setBillingType("SUBSCRIPTION");
        api.setUnit("API_CALL");
        api.setMonthlyFee(new BigDecimal("50"));
        api.setIncludedUnits(1_000_000L);
        api.setOverageUnitPrice(new BigDecimal("0.001"));

        properties.getPricing().put("storage", storage);
        properties.getPricing().put("compute", compute);
        properties.getPricing().put("api", api);
        return properties;
    }

    private static UsageEvent usage(
            String userId,
            String resourceId,
            String serviceType,
            String unit,
            String quantity,
            String timestamp) {
        return new UsageEvent(
                userId,
                resourceId,
                ServiceKey.of(serviceType),
                UnitKey.of(unit),
                UsageQuantity.of(new BigDecimal(quantity)),
                Instant.parse(timestamp));
    }
}
