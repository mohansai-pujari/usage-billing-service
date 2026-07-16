package com.billing.support;

import com.billing.config.BillingProperties;
import com.billing.pricing.BillingCalculator;
import com.billing.pricing.InvoiceAssembler;
import com.billing.pricing.UsageAggregator;
import com.billing.pricing.currency.InvoiceCurrencyConverter;
import com.billing.pricing.registry.PricingConfigurationRegistry;
import com.billing.pricing.strategy.PricingStrategy;
import com.billing.pricing.strategy.impl.FlatPricingStrategy;
import com.billing.pricing.strategy.impl.SubscriptionPricingStrategy;
import com.billing.pricing.strategy.impl.TieredPricingStrategy;
import com.billing.pricing.strategy.registry.PricingStrategyRegistry;
import com.billing.service.BillingService;
import com.billing.storage.UsageStore;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.List;

public final class BillingContextBuilder {

    private BillingProperties properties = DemoPricingProperties.create();

    private BillingContextBuilder() {
    }

    public static BillingContextBuilder create() {
        return new BillingContextBuilder();
    }

    public BillingContextBuilder withDemoPricing() {
        this.properties = DemoPricingProperties.create();
        return this;
    }

    public BillingContextBuilder withPricing(BillingProperties properties) {
        this.properties = properties;
        return this;
    }

    public BillingContext build() {
        PricingStrategyRegistry strategyRegistry = new PricingStrategyRegistry(defaultStrategies());
        PricingConfigurationRegistry pricingRegistry = new PricingConfigurationRegistry(properties, strategyRegistry);
        UsageStore usageStore = new UsageStore();
        InvoiceAssembler invoiceAssembler = new InvoiceAssembler(
                new UsageAggregator(),
                new BillingCalculator(strategyRegistry),
                pricingRegistry);
        BillingMetrics billingMetrics = new BillingMetrics(new SimpleMeterRegistry());
        BillingService billingService = new BillingService(
                usageStore,
                pricingRegistry,
                invoiceAssembler,
                new InvoiceCurrencyConverter(properties),
                billingMetrics);
        return new BillingContext(usageStore, billingService);
    }

    private static List<PricingStrategy> defaultStrategies() {
        return List.of(
                new FlatPricingStrategy(),
                new TieredPricingStrategy(),
                new SubscriptionPricingStrategy());
    }
}
