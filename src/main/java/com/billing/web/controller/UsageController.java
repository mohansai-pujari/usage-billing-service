package com.billing.web.controller;

import com.billing.domain.common.BillingPeriod;
import com.billing.service.UsageService;
import com.billing.web.dto.request.UsageRequest;
import com.billing.web.dto.response.UsageAcceptedResponse;
import com.billing.web.dto.response.UsageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/** HTTP endpoints for usage ingestion and listing. */
@RestController
@Tag(name = "Usage", description = "Record and query resource usage events")
public class UsageController {

    private static final Logger log = LoggerFactory.getLogger(UsageController.class);

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @PostMapping("/usage")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a usage event")
    public UsageAcceptedResponse recordUsage(@Valid @RequestBody UsageRequest request) {
        log.debug("POST /usage received for user={}", request.userId());
        usageService.recordUsage(request);
        return new UsageAcceptedResponse("Usage recorded successfully");
    }

    @GetMapping("/usages")
    @Operation(summary = "List usage events for a billing period")
    public List<UsageResponse> listUsages(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String serviceType,
            @RequestParam long start,
            @RequestParam long end) {

        log.debug("GET /usages userId={}, serviceType={}, period=[{}, {})", userId, serviceType, start, end);
        BillingPeriod period = new BillingPeriod(start, end);
        return usageService.listUsages(userId, serviceType, period).stream()
                .map(UsageResponse::from)
                .toList();
    }
}
