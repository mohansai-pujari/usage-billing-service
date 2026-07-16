package com.billing.config;

import com.billing.pricing.BillingCalculator;
import com.billing.pricing.InvoiceAssembler;
import com.billing.pricing.UsageAggregationStrategy;
import com.billing.pricing.UsageAggregator;
import com.billing.pricing.currency.InvoiceCurrencyConverter;
import com.billing.pricing.registry.PricingConfigurationRegistry;
import com.billing.pricing.strategy.PricingStrategy;
import com.billing.pricing.strategy.impl.FlatPricingStrategy;
import com.billing.pricing.strategy.impl.SubscriptionPricingStrategy;
import com.billing.pricing.strategy.impl.TieredPricingStrategy;
import com.billing.pricing.strategy.registry.PricingStrategyRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Wires billing and pricing components. Pricing logic classes remain Spring-free. */
@Configuration(proxyBeanMethods = false)
public class BillingBeanConfig {

    @Bean
    PricingStrategy flatPricingStrategy() {
        return new FlatPricingStrategy();
    }

    @Bean
    PricingStrategy tieredPricingStrategy() {
        return new TieredPricingStrategy();
    }

    @Bean
    PricingStrategy subscriptionPricingStrategy() {
        return new SubscriptionPricingStrategy();
    }

    @Bean
    PricingStrategyRegistry pricingStrategyRegistry(List<PricingStrategy> strategies) {
        return new PricingStrategyRegistry(strategies);
    }

    @Bean
    PricingConfigurationRegistry pricingConfigurationRegistry(
            BillingProperties billingProperties,
            PricingStrategyRegistry pricingStrategyRegistry) {
        return new PricingConfigurationRegistry(billingProperties, pricingStrategyRegistry);
    }

    @Bean
    UsageAggregationStrategy usageAggregationStrategy() {
        return new UsageAggregator();
    }

    @Bean
    BillingCalculator billingCalculator(PricingStrategyRegistry pricingStrategyRegistry) {
        return new BillingCalculator(pricingStrategyRegistry);
    }

    @Bean
    InvoiceAssembler invoiceAssembler(
            UsageAggregationStrategy usageAggregationStrategy,
            BillingCalculator billingCalculator,
            PricingConfigurationRegistry pricingConfigurationRegistry) {
        return new InvoiceAssembler(usageAggregationStrategy, billingCalculator, pricingConfigurationRegistry);
    }

    @Bean
    InvoiceCurrencyConverter invoiceCurrencyConverter(BillingProperties billingProperties) {
        return new InvoiceCurrencyConverter(billingProperties);
    }
}
