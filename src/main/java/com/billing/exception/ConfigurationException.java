package com.billing.exception;

/**
 * Indicates invalid or missing application/pricing configuration (HTTP 500).
 * <p>
 * Responsibility: Raised at startup or when runtime config is inconsistent. Logged at ERROR level;
 * clients receive a generic service-unavailable message.
 */
public class ConfigurationException extends BillingException {

    /**
     * @param message detailed configuration error for server logs
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * @param message detailed configuration error
     * @param cause   underlying parsing or binding cause
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
