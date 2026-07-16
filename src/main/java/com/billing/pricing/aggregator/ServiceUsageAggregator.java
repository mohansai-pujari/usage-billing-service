package com.billing.pricing.aggregator;

import com.billing.entity.usage.ResourceUsageSummary;
import com.billing.entity.usage.ServiceUsageSummary;

import java.util.List;

public interface ServiceUsageAggregator {

    List<ServiceUsageSummary> aggregate(
            List<ResourceUsageSummary> resourceUsageSummaries);

}