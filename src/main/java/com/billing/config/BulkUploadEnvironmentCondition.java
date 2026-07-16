package com.billing.config;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.List;

public final class BulkUploadEnvironmentCondition implements Condition {

    static final List<String> DEFAULT_ALLOWED = List.of("local", "dev");

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return isEnabled(context.getEnvironment());
    }

    static boolean isEnabled(Environment environment) {
        List<String> allowed = Binder.get(environment)
                .bind("billing.test.bulk-upload.allowed-environments", Bindable.listOf(String.class))
                .orElse(DEFAULT_ALLOWED);

        if (allowed.isEmpty()) {
            return false;
        }

        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return matchesProfile(allowed, "local");
        }

        for (String profile : activeProfiles) {
            if (matchesProfile(allowed, profile)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesProfile(List<String> allowed, String profile) {
        return allowed.stream().anyMatch(env -> env.equalsIgnoreCase(profile));
    }
}
