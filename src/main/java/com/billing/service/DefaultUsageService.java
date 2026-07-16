package com.billing.service;

import com.billing.dto.request.UsageRequest;
import com.billing.entity.common.BillingPeriod;
import com.billing.entity.common.ResourceId;
import com.billing.entity.common.UsageQuantity;
import com.billing.entity.common.UserId;
import com.billing.entity.usage.UsageEvent;
import com.billing.enums.ServiceType;
import com.billing.exception.InvalidRequestException;
import com.billing.repository.usage.UsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class DefaultUsageService implements UsageService {

    private static final Logger log = LoggerFactory.getLogger(DefaultUsageService.class);

    private final BillingService billingService;
    private final UsageRepository usageRepository;

    public DefaultUsageService(BillingService billingService, UsageRepository usageRepository) {
        this.billingService = Objects.requireNonNull(billingService, "Billing service cannot be null.");
        this.usageRepository = Objects.requireNonNull(usageRepository, "Usage repository cannot be null.");
    }

    @Override
    public void recordUsage(UsageRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Usage request cannot be null.");
        }
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new InvalidRequestException("User id is required.");
        }
        if (request.getResourceId() == null || request.getResourceId().isBlank()) {
            throw new InvalidRequestException("Resource id is required.");
        }
        if (request.getServiceType() == null) {
            throw new InvalidRequestException("Service type is required.");
        }
        if (request.getTimestamp() == null) {
            throw new InvalidRequestException("Timestamp is required.");
        }

        log.debug("Creating usage event for user {} and resource {}", request.getUserId(), request.getResourceId());

        UsageEvent usageEvent = new UsageEvent(
                UserId.of(request.getUserId()),
                ResourceId.of(request.getResourceId()),
                request.getServiceType(),
                request.getUnitType(),
                UsageQuantity.of(request.getQuantity()),
                request.getTimestamp()
        );
        billingService.recordUsage(usageEvent);
    }

    @Override
    public List<UsageEvent> listUsages(String userId, ServiceType serviceType, BillingPeriod billingPeriod) {
        if (billingPeriod == null) {
            throw new InvalidRequestException("Billing period cannot be null.");
        }

        List<UsageEvent> events;
        if (userId == null || userId.isBlank()) {
            events = usageRepository.findAll().stream()
                    .filter(event -> billingPeriod.contains(event.getTimestamp()))
                    .toList();
        } else {
            events = usageRepository.findByUserAndPeriod(UserId.of(userId), billingPeriod);
        }

        if (serviceType != null) {
            events = events.stream()
                    .filter(event -> Objects.equals(event.getServiceType(), serviceType))
                    .toList();
        }

        log.debug("Resolved {} usage events for userId={}, serviceType={}, period=[{}, {}]",
                events.size(), userId, serviceType, billingPeriod.getStart(), billingPeriod.getEnd());
        return events;
    }
}
