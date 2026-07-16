package com.billing.web.dto.response;

import com.billing.application.query.UsagePage;
import com.billing.domain.usage.UsageEvent;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated usage event results")
public record UsagePageResponse(
        @Schema(description = "Usage events on the requested page") List<UsageResponse> content,
        @Schema(description = "Zero-based page index", example = "0") int page,
        @Schema(description = "Requested page size", example = "20") int size,
        @Schema(description = "Total matching events", example = "42") long totalElements,
        @Schema(description = "Total available pages", example = "3") int totalPages
) {
    public static UsagePageResponse from(UsagePage<UsageEvent> page) {
        return new UsagePageResponse(
                page.content().stream().map(UsageResponse::from).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }
}
