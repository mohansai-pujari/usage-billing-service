package com.billing.exception;

import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;

/**
 * Thrown when a usage event's unit does not match the expected unit for its service type.
 */
public class ServiceTypeUnitMismatchException extends InvalidRequestException {

    public ServiceTypeUnitMismatchException(String message) {
        super(message);
    }

    public static ServiceTypeUnitMismatchException forPair(ServiceType serviceType, UnitType unit) {
        return new ServiceTypeUnitMismatchException(
                "Unit " + unit + " is not valid for service " + serviceType
                        + ". Expected " + serviceType.expectedUnit() + ".");
    }
}
