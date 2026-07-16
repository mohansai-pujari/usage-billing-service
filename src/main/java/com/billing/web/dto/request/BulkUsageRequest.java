package com.billing.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.List;

@Schema(description = "Bulk usage ingest request for internal testing")
public record BulkUsageRequest(
        @Schema(
                description = "Usage events to record. Omit, send null, or send [] to load classpath test-data/usage-events.json",
                example = "[]")
        List<@Valid UsageRequest> events
) {
    public boolean isEmpty() {
        return events == null || events.isEmpty();
    }
}
