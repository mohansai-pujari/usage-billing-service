package com.billing.repository.usage;

import com.billing.entity.common.BillingPeriod;
import com.billing.entity.common.ResourceId;
import com.billing.entity.common.UserId;
import com.billing.entity.usage.UsageEvent;

import java.util.List;

public interface UsageRepository {

    void save(UsageEvent usageEvent);

    List<UsageEvent> findByUser(UserId userId);

    List<UsageEvent> findByUserAndPeriod(
            UserId userId,
            BillingPeriod billingPeriod);

    List<UsageEvent> findByUserAndResource(
            UserId userId,
            ResourceId resourceId);

    List<UsageEvent> findAll();
}