package com.billing.service;

import com.billing.assembler.InvoiceAssembler;
import com.billing.entity.common.BillingPeriod;
import com.billing.entity.common.UserId;
import com.billing.entity.invoice.CalculatedCharge;
import com.billing.entity.invoice.Invoice;
import com.billing.entity.usage.ResourceUsageSummary;
import com.billing.entity.usage.ServiceUsageSummary;
import com.billing.entity.usage.UsageEvent;
import com.billing.exception.InvalidRequestException;
import com.billing.exception.ResourceNotFoundException;
import com.billing.pricing.aggregator.ServiceUsageAggregator;
import com.billing.pricing.aggregator.UsageAggregator;
import com.billing.pricing.calculator.BillingCalculator;
import com.billing.repository.usage.UsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultBillingService implements BillingService {

    private static final Logger log = LoggerFactory.getLogger(DefaultBillingService.class);

    private final UsageRepository usageRepository;
    private final UsageAggregator usageAggregator;
    private final ServiceUsageAggregator serviceUsageAggregator;
    private final BillingCalculator billingCalculator;
    private final InvoiceAssembler invoiceAssembler;

    public DefaultBillingService(UsageRepository usageRepository,
                                 UsageAggregator usageAggregator,
                                 ServiceUsageAggregator serviceUsageAggregator,
                                 BillingCalculator billingCalculator,
                                 InvoiceAssembler invoiceAssembler) {
        this.usageRepository = usageRepository;
        this.usageAggregator = usageAggregator;
        this.serviceUsageAggregator = serviceUsageAggregator;
        this.billingCalculator = billingCalculator;
        this.invoiceAssembler = invoiceAssembler;
    }

    @Override
    public void recordUsage(UsageEvent usageEvent) {
        if (usageEvent == null) {
            throw new InvalidRequestException("Usage event cannot be null.");
        }
        log.debug("Persisting usage event for user {} and resource {}", usageEvent.getUserId(), usageEvent.getResourceId());
        usageRepository.save(usageEvent);
    }

    @Override
    public Invoice generateInvoice(UserId userId, BillingPeriod billingPeriod) {
        if (userId == null) {
            throw new InvalidRequestException("User id cannot be null.");
        }
        if (billingPeriod == null) {
            throw new InvalidRequestException("Billing period cannot be null.");
        }

        log.debug("Generating invoice for user {} for period [{} - {}]", userId, billingPeriod.getStart(), billingPeriod.getEnd());
        List<UsageEvent> events = usageRepository.findByUserAndPeriod(userId, billingPeriod);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException("No usage found for user " + userId + " in the requested period.");
        }

        log.debug("Found {} usage events for invoice generation", events.size());
        List<ResourceUsageSummary> resourceSummaries = usageAggregator.aggregate(events);
        List<ServiceUsageSummary> serviceSummaries = serviceUsageAggregator.aggregate(resourceSummaries);
        List<CalculatedCharge> charges = billingCalculator.calculate(serviceSummaries);
        log.debug("Calculated {} charges for invoice generation", charges.size());
        return invoiceAssembler.assemble(userId, billingPeriod, charges);
    }
}
