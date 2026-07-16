package com.billing.storage;

import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.domain.usage.UsageEvent;
import com.billing.support.TestEvents;
import com.billing.support.TestTimestamps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsageStoreTest {

    private UsageStore usageStore;

    @BeforeEach
    void setUp() {
        usageStore = new UsageStore();
    }

    @Test
    void storageKeyIncludesServiceType() {
        assertEquals(
                "user-1::disk-1::STORAGE",
                UsageStore.storageKey("user-1", "disk-1", ServiceType.STORAGE));
    }

    @Test
    void keepsSameResourceSeparateAcrossServices() {
        usageStore.save(TestEvents.event(
                "user-1", "shared-1", ServiceType.STORAGE, UnitType.GB_HOUR, "10", TestTimestamps.JAN_10_2026_10_00));
        usageStore.save(TestEvents.event(
                "user-1", "shared-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "20", TestTimestamps.JAN_10_2026_10_00));

        assertEquals(2, usageStore.findAll().size());
    }

    @Test
    void ignoresDuplicateEventIds() {
        UsageEvent first = UsageEvent.of(
                "user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "10",
                TestTimestamps.JAN_10_2026_10_00, "evt-1");
        UsageEvent duplicate = UsageEvent.of(
                "user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "99",
                TestTimestamps.JAN_10_2026_10_00, "evt-1");

        assertTrue(usageStore.save(first));
        assertFalse(usageStore.save(duplicate));
        assertEquals(1, usageStore.findAll().size());
        assertEquals("10", usageStore.findAll().get(0).quantity().asBigDecimal().stripTrailingZeros().toPlainString());
    }
}
