package com.billing.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Acknowledgement returned after a usage event is recorded")
public record UsageAcceptedResponse(
        @Schema(example = "Usage recorded successfully") String message
) {
}
