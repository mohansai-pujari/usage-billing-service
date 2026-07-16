package com.billing.web.handler;

/** Generic, client-safe error messages for API responses. */
public final class ApiErrorMessages {

    private ApiErrorMessages() {
    }

    public static final String INVALID_REQUEST =
            "The request could not be processed. Please verify your input and try again.";

    public static final String NOT_FOUND =
            "The requested resource could not be found.";

    public static final String VALIDATION_FAILED =
            "One or more request fields are invalid.";

    public static final String MALFORMED_BODY =
            "The request body is invalid or malformed.";

    public static final String MISSING_PARAMETER =
            "Required request parameters are missing.";

    public static final String INVALID_PARAMETER =
            "One or more request parameters are invalid.";

    public static final String INTERNAL_ERROR =
            "An unexpected error occurred. Please try again later.";

    public static final String CONFIGURATION_ERROR =
            "The service is temporarily unavailable. Please try again later.";
}
