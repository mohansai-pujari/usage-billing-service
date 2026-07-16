package com.billing.web.dto.response;

/** Standard error body returned by {@link com.billing.web.handler.ApiErrorHandler}. */
public record ErrorResponse(
        long timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
