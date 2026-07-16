package com.billing.pricing;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.common.Money;
import com.billing.domain.common.ServiceKey;
import com.billing.domain.invoice.Invoice;
import com.billing.domain.pricing.PricingConfig;
import com.billing.domain.usage.ResourceUsageSummary;
import com.billing.domain.usage.ServiceUsageSummary;
import com.billing.domain.usage.UsageEvent;
import com.billing.exception.InvalidRequestException;
import com.billing.pricing.registry.PricingConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Assembles domain {@link Invoice} objects from aggregated usage and calculated charges. */
public class InvoiceAssembler {

    private static final Logger log = LoggerFactory.getLogger(InvoiceAssembler.class);

    private final UsageAggregationStrategy usageAggregator;
    private final BillingCalculator billingCalculator;
    private final PricingConfigurationRegistry pricingRegistry;

    public InvoiceAssembler(
            UsageAggregationStrategy usageAggregator,
            BillingCalculator billingCalculator,
            PricingConfigurationRegistry pricingRegistry) {
        this.usageAggregator = usageAggregator;
        this.billingCalculator = billingCalculator;
        this.pricingRegistry = pricingRegistry;
    }

    public Invoice assemble(String userId, BillingPeriod period, List<UsageEvent> events) {
        requireText(userId, "User id");
        Objects.requireNonNull(period, "Billing period cannot be null.");
        if (events == null || events.isEmpty()) {
            throw new InvalidRequestException("At least one usage event is required to build an invoice.");
        }

        log.debug("Assembling invoice for user={} from {} usage events", userId, events.size());

        List<ServiceUsageSummary> serviceUsages = usageAggregator.aggregate(events);
        List<Invoice.LineItem> lineItems = new ArrayList<>();
        Map<ServiceKey, List<Invoice.LineItem>> linesByService = new LinkedHashMap<>();
        Map<ServiceKey, Money> totalsByService = new LinkedHashMap<>();

        for (ServiceUsageSummary usage : serviceUsages) {
            PricingConfig config = pricingRegistry.getConfig(usage.serviceType());
            pricingRegistry.validateUnit(usage.serviceType(), usage.unit());

            Money serviceCharge = billingCalculator.calculateServiceCharge(config, usage);
            totalsByService.merge(usage.serviceType(), serviceCharge, Money::add);

            List<Money> resourceAmounts = billingCalculator.calculateResourceLineAmounts(config, usage, serviceCharge);
            List<ResourceUsageSummary> resources = usage.resources();
            for (int index = 0; index < resources.size(); index++) {
                ResourceUsageSummary resource = resources.get(index);
                Invoice.LineItem lineItem = new Invoice.LineItem(
                        resource.resourceId(),
                        usage.serviceType().value() + " usage",
                        resource.quantity(),
                        usage.unit(),
                        resourceAmounts.get(index));
                lineItems.add(lineItem);
                linesByService.computeIfAbsent(usage.serviceType(), ignored -> new ArrayList<>()).add(lineItem);
            }
        }

        List<Invoice.ServiceSubtotal> subtotals = new ArrayList<>();
        Money total = Money.zero();
        for (Map.Entry<ServiceKey, Money> entry : totalsByService.entrySet()) {
            total = total.add(entry.getValue());
            subtotals.add(new Invoice.ServiceSubtotal(
                    entry.getKey(),
                    entry.getValue(),
                    List.copyOf(linesByService.get(entry.getKey()))));
        }

        log.debug("Invoice assembled for user={} with total={}", userId, total);
        return new Invoice(userId, period, List.copyOf(lineItems), List.copyOf(subtotals), total);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestException(field + " is required.");
        }
    }
}
