package com.billing.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a bulk usage ingest (internal testing)")
public record BulkUsageAcceptedResponse(
        @Schema(example = "Bulk usage recorded successfully") String message,
        @Schema(description = "Number of new events stored", example = "4") int accepted,
        @Schema(description = "Number of duplicate eventIds skipped", example = "0") int skippedDuplicates,
        @Schema(description = "Where events were loaded from", example = "test-data/usage-events.json") String source,
        @Schema(description = "Total events processed", example = "4") int totalProcessed
) {
}
