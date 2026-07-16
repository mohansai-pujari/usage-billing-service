package com.billing.pricing.aggregator;

import com.billing.entity.usage.UsageEvent;
import com.billing.entity.usage.ResourceUsageSummary;

import java.util.List;

public interface UsageAggregator {

    List<ResourceUsageSummary> aggregate(List<UsageEvent> usageEvents);

}