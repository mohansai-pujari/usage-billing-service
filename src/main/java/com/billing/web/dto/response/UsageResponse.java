package com.billing.web.dto.response;

import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.domain.usage.UsageEvent;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usage event returned by the billing API")
public record UsageResponse(
        @Schema(example = "user-1") String userId,
        @Schema(example = "disk-1") String resourceId,
        @Schema(example = "STORAGE") ServiceType serviceType,
        @Schema(example = "GB_HOUR") UnitType unit,
        @Schema(example = "100") String quantity,
        @Schema(example = "1768039200000") long timestamp,
        @Schema(description = "Idempotency key when provided on ingest", example = "evt-20260110-disk-1") String eventId
) {
    public static UsageResponse from(UsageEvent event) {
        return new UsageResponse(
                event.userId(),
                event.resourceId(),
                event.serviceType(),
                event.unit(),
                event.quantity().asBigDecimal().stripTrailingZeros().toPlainString(),
                event.timestamp(),
                event.eventId());
    }
}
