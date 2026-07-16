package com.billing.exception;

/**
 * Indicates invalid client input or domain validation failure (HTTP 400).
 * <p>
 * Responsibility: Signals recoverable request errors. Mapped to a generic bad-request message by
 * {@link com.billing.web.handler.ApiErrorHandler}; detailed text is logged only.
 */
public class InvalidRequestException extends BillingException {

    /**
     * @param message detailed internal reason (logged, not returned to client)
     */
    public InvalidRequestException(String message) {
        super(message);
    }

    /**
     * @param message detailed internal reason
     * @param cause   underlying validation or parsing cause
     */
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
