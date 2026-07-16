package com.billing.web.dto.request;

import com.billing.domain.common.ValidationRules;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.domain.usage.UsageEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Usage event submitted for billing")
public record UsageRequest(
        @Schema(description = "Billable user identifier", example = "user-1")
        @NotBlank @Size(max = ValidationRules.MAX_ID_LENGTH) String userId,

        @Schema(description = "Resource identifier within the user's account", example = "disk-1")
        @NotBlank @Size(max = ValidationRules.MAX_ID_LENGTH) String resourceId,

        @Schema(description = "Billable service", example = "STORAGE", allowableValues = {"STORAGE", "COMPUTE", "API"})
        @NotNull ServiceType serviceType,

        @Schema(description = "Unit of measure for the service", example = "GB_HOUR", allowableValues = {"GB_HOUR", "COMPUTE_HOUR", "API_CALL"})
        @NotNull UnitType unit,

        @Schema(description = "Positive usage quantity", example = "100.0")
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,

        @Schema(description = "Event timestamp in epoch milliseconds", example = "1768039200000")
        @Min(0) long timestamp,

        @Schema(description = "Optional idempotency key; duplicate eventIds are ignored", example = "evt-20260110-disk-1")
        @Size(max = ValidationRules.MAX_ID_LENGTH) String eventId
) {
    public UsageEvent toEvent() {
        return UsageEvent.of(userId, resourceId, serviceType, unit, quantity, timestamp, eventId);
    }
}
