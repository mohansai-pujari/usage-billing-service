package com.billing.service;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.enums.CurrencyType;
import com.billing.domain.invoice.Invoice;
import com.billing.domain.usage.UsageEvent;
import com.billing.exception.InvalidRequestException;
import com.billing.exception.ResourceNotFoundException;
import com.billing.pricing.InvoiceAssembler;
import com.billing.pricing.currency.InvoiceCurrencyConverter;
import com.billing.pricing.registry.PricingConfigurationRegistry;
import com.billing.storage.UsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/** Orchestrates usage persistence and invoice generation. */
@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final UsageRepository usageRepository;
    private final InvoiceAssembler invoiceAssembler;
    private final PricingConfigurationRegistry pricingRegistry;
    private final InvoiceCurrencyConverter invoiceCurrencyConverter;

    public BillingService(
            UsageRepository usageRepository,
            InvoiceAssembler invoiceAssembler,
            PricingConfigurationRegistry pricingRegistry,
            InvoiceCurrencyConverter invoiceCurrencyConverter) {
        this.usageRepository = Objects.requireNonNull(usageRepository);
        this.invoiceAssembler = Objects.requireNonNull(invoiceAssembler);
        this.pricingRegistry = Objects.requireNonNull(pricingRegistry);
        this.invoiceCurrencyConverter = Objects.requireNonNull(invoiceCurrencyConverter);
    }

    public void recordUsage(UsageEvent event) {
        if (event == null) {
            throw new InvalidRequestException("Usage event cannot be null.");
        }

        pricingRegistry.validateUnit(event.serviceType(), event.unit());
        log.debug("Recording usage for user={}, resource={}, service={}",
                event.userId(), event.resourceId(), event.serviceType());
        usageRepository.save(event);
    }

    public Invoice generateInvoice(String userId, BillingPeriod period) {
        return generateInvoice(userId, period, CurrencyType.USD);
    }

    public Invoice generateInvoice(String userId, BillingPeriod period, CurrencyType currency) {
        if (userId == null || userId.isBlank()) {
            throw new InvalidRequestException("User id is required.");
        }
        if (period == null) {
            throw new InvalidRequestException("Billing period cannot be null.");
        }
        if (currency == null) {
            throw new InvalidRequestException("Currency type is required.");
        }

        String trimmedUserId = userId.trim();
        log.debug("Generating invoice for user={} period=[{}, {}) currency={}",
                trimmedUserId, period.start(), period.end(), currency);

        List<UsageEvent> events = usageRepository.findByUserAndPeriod(trimmedUserId, period);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No usage found for user " + trimmedUserId
                            + " in period [" + period.start() + ", " + period.end() + ").");
        }

        Invoice invoice = invoiceCurrencyConverter.convert(
                invoiceAssembler.assemble(trimmedUserId, period, events),
                currency);
        log.debug("Generated invoice for user={} total={}", trimmedUserId, invoice.total().format(currency));
        return invoice;
    }
}
