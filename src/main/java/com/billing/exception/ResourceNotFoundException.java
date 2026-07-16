package com.billing.exception;

/**
 * Indicates a requested resource does not exist (HTTP 404).
 * <p>
 * Responsibility: Raised when usage or pricing configuration cannot be found for a valid request.
 * Mapped to a generic not-found message by {@link com.billing.web.handler.ApiErrorHandler}.
 */
public class ResourceNotFoundException extends BillingException {

    /**
     * @param message detailed internal reason (logged, not returned to client)
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * @param message detailed internal reason
     * @param cause   underlying cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
