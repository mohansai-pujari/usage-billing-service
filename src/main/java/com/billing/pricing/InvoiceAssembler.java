package com.billing.pricing;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.common.Money;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.invoice.Invoice;
import com.billing.domain.pricing.PricingConfig;
import com.billing.domain.usage.ResourceUsageSummary;
import com.billing.domain.usage.ServiceUsageSummary;
import com.billing.domain.usage.UsageEvent;
import com.billing.exception.InvalidRequestException;
import com.billing.pricing.registry.PricingConfigurationRegistry;
import com.billing.support.LogLabels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class InvoiceAssembler {

    private static final Logger log = LoggerFactory.getLogger(InvoiceAssembler.class);

    @Autowired
    private UsageAggregator usageAggregator;

    @Autowired
    private BillingCalculator billingCalculator;

    @Autowired
    private PricingConfigurationRegistry pricingRegistry;

    public InvoiceAssembler() {
    }

    public InvoiceAssembler(
            UsageAggregator usageAggregator,
            BillingCalculator billingCalculator,
            PricingConfigurationRegistry pricingRegistry) {
        this.usageAggregator = usageAggregator;
        this.billingCalculator = billingCalculator;
        this.pricingRegistry = pricingRegistry;
    }

    public Invoice assemble(String userId, BillingPeriod period, List<UsageEvent> events) {
        Objects.requireNonNull(period, "Billing period cannot be null.");
        if (events == null || events.isEmpty()) {
            throw new InvalidRequestException("At least one usage event is required to build an invoice.");
        }

        log.debug("Assembling invoice for user={} from {} usage events",
                LogLabels.userId(userId),
                events.size());

        Map<ServiceType, List<Invoice.LineItem>> linesByService = new LinkedHashMap<>();
        Map<ServiceType, Money> totalsByService = new LinkedHashMap<>();

        for (ServiceUsageSummary usage : usageAggregator.aggregate(events)) {
            PricingConfig config = pricingRegistry.getConfig(usage.serviceType());
            pricingRegistry.validateUnit(usage.serviceType(), usage.unit());

            Money serviceCharge = billingCalculator.calculateServiceCharge(config, usage);
            totalsByService.merge(usage.serviceType(), serviceCharge, Money::add);
            linesByService.computeIfAbsent(usage.serviceType(), ignored -> new ArrayList<>())
                    .addAll(buildLineItems(usage, billingCalculator.calculateResourceLineAmounts(config, usage, serviceCharge)));
        }

        List<Invoice.LineItem> lineItems = linesByService.values().stream()
                .flatMap(List::stream)
                .toList();

        List<Invoice.ServiceSubtotal> subtotals = new ArrayList<>();
        Money total = Money.zero();
        for (Map.Entry<ServiceType, Money> entry : totalsByService.entrySet()) {
            total = total.add(entry.getValue());
            subtotals.add(new Invoice.ServiceSubtotal(
                    entry.getKey(),
                    entry.getValue(),
                    List.copyOf(linesByService.get(entry.getKey()))));
        }

        log.debug("Invoice assembled for user={} with total={}", LogLabels.userId(userId), total);
        return new Invoice(userId, period, lineItems, List.copyOf(subtotals), total);
    }

    private static List<Invoice.LineItem> buildLineItems(ServiceUsageSummary usage, List<Money> resourceAmounts) {
        List<ResourceUsageSummary> resources = usage.resources();
        List<Invoice.LineItem> lineItems = new ArrayList<>(resources.size());
        for (int index = 0; index < resources.size(); index++) {
            ResourceUsageSummary resource = resources.get(index);
            lineItems.add(new Invoice.LineItem(
                    resource.resourceId(),
                    usage.serviceType() + " usage",
                    resource.quantity(),
                    usage.unit(),
                    resourceAmounts.get(index)));
        }
        return lineItems;
    }
}
