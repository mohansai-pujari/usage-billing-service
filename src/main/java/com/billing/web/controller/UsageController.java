package com.billing.web.controller;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.enums.ServiceType;
import com.billing.service.UsageService;
import com.billing.web.dto.request.UsageRequest;
import com.billing.web.dto.response.UsageAcceptedResponse;
import com.billing.web.dto.response.UsagePageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Usage", description = "Record and query resource usage events")
public class UsageController {

    private static final Logger log = LoggerFactory.getLogger(UsageController.class);

    @Autowired
    private UsageService usageService;

    @PostMapping("/usage")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a usage event")
    public UsageAcceptedResponse recordUsage(@Valid @RequestBody UsageRequest request) {
        log.debug("POST /usage user={}", request.userId());
        usageService.recordUsage(request);
        return new UsageAcceptedResponse("Usage recorded successfully");
    }

    @GetMapping("/usages")
    @Operation(summary = "List usage events for a billing period")
    public UsagePageResponse listUsages(
            @Parameter(description = "Filter by user; omit for all users")
            @RequestParam(required = false) String userId,
            @Parameter(description = "Filter by service type")
            @RequestParam(required = false) ServiceType serviceType,
            @RequestParam long start,
            @RequestParam long end,
            @Parameter(description = "Zero-based page index; omit with size to return all results")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100); omit with page to return all results")
            @RequestParam(required = false) Integer size) {

        log.debug("GET /usages userId={}, serviceType={}, period=[{}, {}), page={}, size={}",
                userId, serviceType, start, end, page, size);
        return UsagePageResponse.from(
                usageService.listUsages(userId, serviceType, new BillingPeriod(start, end), page, size));
    }
}
