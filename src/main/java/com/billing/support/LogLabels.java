package com.billing.support;

import com.billing.domain.enums.ServiceType;

public final class LogLabels {

    private LogLabels() {
    }

    public static String userId(String userId) {
        return userId != null && !userId.isBlank() ? userId : "ALL";
    }

    public static String serviceType(ServiceType serviceType) {
        return serviceType != null ? serviceType.name() : "ALL";
    }
}
