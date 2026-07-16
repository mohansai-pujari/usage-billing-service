package com.billing.exception;

/**
 * Base runtime exception for all billing-domain and application errors.
 * <p>
 * Responsibility: Root of the typed exception hierarchy handled by {@link com.billing.web.handler.ApiErrorHandler}.
 * Internal messages are logged server-side; clients receive generic messages from {@link com.billing.web.handler.ApiErrorMessages}.
 * <p>
 * Design patterns: Exception Hierarchy — enables centralized handling by exception type.
 */
public class BillingException extends RuntimeException {

    /**
     * Creates an exception with a detailed internal message for logging.
     *
     * @param message internal error description (not exposed to API clients)
     */
    public BillingException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a cause for startup or configuration failures.
     *
     * @param message internal error description
     * @param cause   underlying cause
     */
    public BillingException(String message, Throwable cause) {
        super(message, cause);
    }
}
