package com.billing.support;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BillingMetrics {

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter usageRecorded;
    private Counter usageDuplicate;
    private Counter invoicesGenerated;

    public BillingMetrics() {
    }

    public BillingMetrics(MeterRegistry meterRegistry) {
        registerCounters(meterRegistry);
    }

    @PostConstruct
    void initialize() {
        registerCounters(meterRegistry);
    }

    public void recordUsageSaved() {
        usageRecorded.increment();
    }

    public void recordUsageDuplicate() {
        usageDuplicate.increment();
    }

    public void recordInvoiceGenerated() {
        invoicesGenerated.increment();
    }

    private void registerCounters(MeterRegistry meterRegistry) {
        this.usageRecorded = meterRegistry.counter("billing.usage.recorded");
        this.usageDuplicate = meterRegistry.counter("billing.usage.duplicate");
        this.invoicesGenerated = meterRegistry.counter("billing.invoice.generated");
    }
}
