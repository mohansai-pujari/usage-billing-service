package com.billing.web.dto.request;

import com.billing.domain.common.ValidationRules;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/** Incoming usage event payload for {@code POST /usage}. */
public record UsageRequest(
        @NotBlank @Size(max = ValidationRules.MAX_ID_LENGTH) String userId,
        @NotBlank @Size(max = ValidationRules.MAX_ID_LENGTH) String resourceId,
        @NotBlank String serviceType,
        @NotBlank String unit,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
        @NotNull Instant timestamp
) {
}
