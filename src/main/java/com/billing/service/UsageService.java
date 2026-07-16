package com.billing.service;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.common.ServiceKey;
import com.billing.domain.common.UnitKey;
import com.billing.domain.common.UsageQuantity;
import com.billing.domain.usage.UsageEvent;
import com.billing.exception.InvalidRequestException;
import com.billing.storage.UsageRepository;
import com.billing.web.dto.request.UsageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/** Application service for usage ingestion and querying. */
@Service
public class UsageService {

    private static final Logger log = LoggerFactory.getLogger(UsageService.class);

    private final BillingService billingService;
    private final UsageRepository usageRepository;

    public UsageService(BillingService billingService, UsageRepository usageRepository) {
        this.billingService = Objects.requireNonNull(billingService);
        this.usageRepository = Objects.requireNonNull(usageRepository);
    }

    public void recordUsage(UsageRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Usage request cannot be null.");
        }

        log.debug("Recording usage for user={}, resource={}, service={}",
                request.userId(), request.resourceId(), request.serviceType());

        billingService.recordUsage(new UsageEvent(
                request.userId(),
                request.resourceId(),
                ServiceKey.of(request.serviceType()),
                UnitKey.of(request.unit()),
                UsageQuantity.of(request.quantity()),
                request.timestamp()));
    }

    public List<UsageEvent> listUsages(String userId, String serviceType, BillingPeriod period) {
        if (period == null) {
            throw new InvalidRequestException("Billing period cannot be null.");
        }

        List<UsageEvent> events = (userId == null || userId.isBlank())
                ? usageRepository.findAll().stream().filter(event -> period.contains(event.timestamp())).toList()
                : usageRepository.findByUserAndPeriod(userId.trim(), period);

        if (serviceType != null && !serviceType.isBlank()) {
            ServiceKey filter = ServiceKey.of(serviceType);
            events = events.stream().filter(event -> event.serviceType().equals(filter)).toList();
        }

        log.debug("Returning {} usage events", events.size());
        return events;
    }
}
