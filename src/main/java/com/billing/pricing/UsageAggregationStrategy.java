package com.billing.pricing;

import com.billing.domain.usage.ServiceUsageSummary;
import com.billing.domain.usage.UsageEvent;

import java.util.List;

/**
 * Aggregates raw usage events into billable summaries.
 * <p>
 * Design patterns: Strategy-like abstraction — alternate aggregators can be plugged in without
 * changing invoice assembly.
 */
public interface UsageAggregationStrategy {

    /**
     * Aggregates events by resource, then rolls up to service-level totals.
     *
     * @param events usage events for one user in a billing period
     * @return service-level summaries with nested resource lines
     */
    List<ServiceUsageSummary> aggregate(List<UsageEvent> events);
}
