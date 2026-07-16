package com.billing.web.controller;

import com.billing.config.BulkUploadEnvironmentCondition;
import com.billing.exception.ConfigurationException;
import com.billing.service.BillingService;
import com.billing.web.dto.request.BulkUsageRequest;
import com.billing.web.dto.request.UsageRequest;
import com.billing.web.dto.response.BulkUsageAcceptedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@Conditional(BulkUploadEnvironmentCondition.class)
@Tag(name = "Internal Testing", description = "Local/manual test helpers exposed in Swagger UI only")
public class BulkUsageController {

    static final String DEFAULT_FIXTURE = "test-data/usage-events.json";

    private static final Logger log = LoggerFactory.getLogger(BulkUsageController.class);

    @Autowired
    private BillingService billingService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/usage/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Bulk record usage (internal testing)",
            description = "Empty body loads test-data/usage-events.json.")
    public BulkUsageAcceptedResponse recordBulkUsage(
            @Valid @RequestBody(required = false) BulkUsageRequest request) {
        log.debug("POST /usage/bulk payloadProvided={}", request != null && !request.isEmpty());

        String source;
        List<UsageRequest> events;
        if (request == null || request.isEmpty()) {
            events = loadDefaultEvents();
            source = DEFAULT_FIXTURE;
            log.debug("Bulk usage ingest loading {} events from {}", events.size(), source);
        } else {
            events = request.events();
            source = "request-payload";
            log.debug("Bulk usage ingest processing {} events from payload", events.size());
        }

        int accepted = 0;
        int skippedDuplicates = 0;
        for (UsageRequest usageRequest : events) {
            if (billingService.recordUsage(usageRequest.toEvent())) {
                accepted++;
            } else {
                skippedDuplicates++;
            }
        }

        return new BulkUsageAcceptedResponse(
                "Bulk usage recorded successfully",
                accepted,
                skippedDuplicates,
                source,
                events.size());
    }

    private List<UsageRequest> loadDefaultEvents() {
        ClassPathResource resource = new ClassPathResource(DEFAULT_FIXTURE);
        if (!resource.exists()) {
            throw new ConfigurationException("Test usage data file not found: " + DEFAULT_FIXTURE);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            BulkUsageRequest payload = objectMapper.readValue(inputStream, BulkUsageRequest.class);
            if (payload == null || payload.isEmpty()) {
                throw new ConfigurationException("Test usage data file is empty: " + DEFAULT_FIXTURE);
            }
            return List.copyOf(payload.events());
        } catch (IOException ex) {
            throw new ConfigurationException("Failed to load test usage data from " + DEFAULT_FIXTURE, ex);
        }
    }
}
