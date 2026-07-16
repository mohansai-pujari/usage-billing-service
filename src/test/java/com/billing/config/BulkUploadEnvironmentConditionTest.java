package com.billing.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class BulkUploadEnvironmentConditionTest {

    @Test
    void shouldEnableWhenNoProfileAndLocalIsAllowed() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("billing.test.bulk-upload.allowed-environments[0]", "local");
        environment.setProperty("billing.test.bulk-upload.allowed-environments[1]", "dev");

        assertThat(BulkUploadEnvironmentCondition.isEnabled(environment)).isTrue();
    }

    @Test
    void shouldMatchAllowedProfilesCaseInsensitively() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("billing.test.bulk-upload.allowed-environments[0]", "local");
        environment.setProperty("billing.test.bulk-upload.allowed-environments[1]", "dev");
        environment.setActiveProfiles("DEV");

        assertThat(BulkUploadEnvironmentCondition.isEnabled(environment)).isTrue();
    }

    @Test
    void shouldDisableWhenActiveProfileNotAllowed() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("billing.test.bulk-upload.allowed-environments[0]", "local");
        environment.setProperty("billing.test.bulk-upload.allowed-environments[1]", "dev");
        environment.setActiveProfiles("prod");

        assertThat(BulkUploadEnvironmentCondition.isEnabled(environment)).isFalse();
    }

    @Test
    void shouldDisableWhenAllowedListIsEmpty() {
        MockEnvironment environment = new MockEnvironment();
        environment.getPropertySources().addFirst(
                new org.springframework.core.env.MapPropertySource(
                        "test",
                        java.util.Map.of("billing.test.bulk-upload.allowed-environments", java.util.List.of())));
        environment.setActiveProfiles("local");

        assertThat(BulkUploadEnvironmentCondition.isEnabled(environment)).isFalse();
    }

    @Test
    void shouldUseDefaultAllowedEnvironmentsWhenPropertyMissing() {
        MockEnvironment environment = new MockEnvironment();

        assertThat(BulkUploadEnvironmentCondition.isEnabled(environment)).isTrue();
    }
}
