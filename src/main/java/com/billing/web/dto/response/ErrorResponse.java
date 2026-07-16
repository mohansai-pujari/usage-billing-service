package com.billing.web.dto.response;

import java.time.Instant;

/** Standard error body returned by {@link com.billing.web.handler.ApiErrorHandler}. */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
