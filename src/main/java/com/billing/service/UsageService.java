package com.billing.service;

import com.billing.application.query.UsagePage;
import com.billing.application.query.UsageQuery;
import com.billing.domain.common.BillingPeriod;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.usage.UsageEvent;
import com.billing.storage.UsageRepository;
import com.billing.web.dto.request.UsageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsageService {

    private static final Logger log = LoggerFactory.getLogger(UsageService.class);

    @Autowired
    private BillingService billingService;

    @Autowired
    private UsageRepository usageRepository;

    public UsageService() {
    }

    public UsageService(BillingService billingService, UsageRepository usageRepository) {
        this.billingService = billingService;
        this.usageRepository = usageRepository;
    }

    public void recordUsage(UsageRequest request) {
        billingService.recordUsage(request.toEvent());
    }

    public UsagePage<UsageEvent> listUsages(
            String userId,
            ServiceType serviceType,
            BillingPeriod period,
            Integer page,
            Integer size) {
        UsagePage<UsageEvent> result = UsagePage.of(
                usageRepository.findByQuery(UsageQuery.of(userId, serviceType, period)),
                page,
                size);
        log.debug("Returning {} usage events (page={}, size={}, total={})",
                result.content().size(), result.page(), result.size(), result.totalElements());
        return result;
    }
}
