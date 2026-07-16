package com.billing.service;

import com.billing.application.query.InvoiceQuery;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.invoice.Invoice;
import com.billing.domain.usage.UsageEvent;
import com.billing.exception.InvalidRequestException;
import com.billing.exception.ResourceNotFoundException;
import com.billing.pricing.InvoiceAssembler;
import com.billing.pricing.currency.InvoiceCurrencyConverter;
import com.billing.pricing.registry.PricingConfigurationRegistry;
import com.billing.storage.UsageRepository;
import com.billing.support.BillingMetrics;
import com.billing.support.LogLabels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    @Autowired
    private UsageRepository usageRepository;

    @Autowired
    private PricingConfigurationRegistry pricingRegistry;

    @Autowired
    private InvoiceAssembler invoiceAssembler;

    @Autowired
    private InvoiceCurrencyConverter invoiceCurrencyConverter;

    @Autowired
    private BillingMetrics billingMetrics;

    public BillingService() {
    }

    public BillingService(
            UsageRepository usageRepository,
            PricingConfigurationRegistry pricingRegistry,
            InvoiceAssembler invoiceAssembler,
            InvoiceCurrencyConverter invoiceCurrencyConverter,
            BillingMetrics billingMetrics) {
        this.usageRepository = usageRepository;
        this.pricingRegistry = pricingRegistry;
        this.invoiceAssembler = invoiceAssembler;
        this.invoiceCurrencyConverter = invoiceCurrencyConverter;
        this.billingMetrics = billingMetrics;
    }

    public boolean recordUsage(UsageEvent event) {
        if (event == null) {
            throw new InvalidRequestException("Usage event cannot be null.");
        }

        pricingRegistry.validateUnit(event.serviceType(), event.unit());
        log.debug("Recording usage for user={}, resource={}, service={}",
                event.userId(), event.resourceId(), event.serviceType());
        if (usageRepository.save(event)) {
            billingMetrics.recordUsageSaved();
            return true;
        }
        billingMetrics.recordUsageDuplicate();
        return false;
    }

    public Invoice generateInvoice(InvoiceQuery query) {
        log.debug("Generating invoice for user={} period=[{}, {}) currency={} serviceType={}",
                LogLabels.userId(query.userId()), query.period().start(), query.period().end(),
                query.currency(), LogLabels.serviceType(query.serviceType()));

        List<UsageEvent> events = usageRepository.findByQuery(query.toUsageQuery());
        if (events.isEmpty()) {
            throw new ResourceNotFoundException(noUsageMessage(query));
        }

        Invoice invoice = invoiceCurrencyConverter.convert(
                invoiceAssembler.assemble(query.userId(), query.period(), events),
                query.currency());
        log.debug("Generated invoice for user={} total={}",
                LogLabels.userId(query.userId()), invoice.total().format(query.currency()));
        billingMetrics.recordInvoiceGenerated();
        return invoice;
    }

    private static String noUsageMessage(InvoiceQuery query) {
        String periodRange = "period [" + query.period().start() + ", " + query.period().end() + ")";
        String userId = query.userId();
        ServiceType serviceType = query.serviceType();

        if (userId != null && serviceType != null) {
            return "No usage found for user " + userId + " and service " + serviceType + " in " + periodRange + ".";
        }
        if (userId != null) {
            return "No usage found for user " + userId + " in " + periodRange + ".";
        }
        if (serviceType != null) {
            return "No usage found for service " + serviceType + " in " + periodRange + ".";
        }
        return "No usage found in " + periodRange + ".";
    }
}
