package com.billing.service;

import com.billing.dto.request.UsageRequest;
import com.billing.entity.common.BillingPeriod;
import com.billing.entity.usage.UsageEvent;
import com.billing.enums.ServiceType;

import java.util.List;

public interface UsageService {

    void recordUsage(UsageRequest request);

    List<UsageEvent> listUsages(String userId, ServiceType serviceType, BillingPeriod billingPeriod);
}
