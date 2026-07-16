package com.billing.controller;

import com.billing.dto.request.UsageRequest;
import com.billing.dto.response.UsageResponse;
import com.billing.entity.common.BillingPeriod;
import com.billing.exception.InvalidRequestException;
import com.billing.service.UsageService;
import com.billing.enums.ServiceType;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UsageController {

    private static final Logger log = LoggerFactory.getLogger(UsageController.class);

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @PostMapping("/usage")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> recordUsage(@Valid @RequestBody UsageRequest request) {

        log.debug("Recording usage for user {} on resource {}", request.getUserId(), request.getResourceId());
        
        usageService.recordUsage(request);
        
        log.debug("Usage recorded successfully for user {}", request.getUserId());
        
        return Map.of("message", "Usage recorded successfully");
    }

    @GetMapping("/usages")
    public List<UsageResponse> listUsages(@RequestParam(required = false) String userId,
                                          @RequestParam(required = false) ServiceType serviceType,
                                          @RequestParam long start,
                                          @RequestParam long end) {

        log.debug("Listing usages with userId={}, serviceType={}, start={}, end={}", userId, serviceType, start, end);

        if (start < 0 || end < 0) {
            throw new InvalidRequestException("Start and end timestamps must be non-negative.");
        }

        BillingPeriod billingPeriod = new BillingPeriod(start, end);
        return usageService.listUsages(userId, serviceType, billingPeriod).stream()
                .map(UsageResponse::from)
                .toList();
    }
}
