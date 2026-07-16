package com.billing.storage;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.usage.UsageEvent;

import java.util.List;

/**
 * Repository abstraction for usage event persistence.
 * <p>
 * Responsibility: Hides storage details from application services so the backing store can be swapped.
 * <p>
 * Design patterns: Repository.
 */
public interface UsageRepository {

    /**
     * Persists a usage event.
     *
     * @param event validated usage event
     */
    void save(UsageEvent event);

    /**
     * Returns usage events for a user whose timestamps fall within the billing period.
     *
     * @param userId billed user
     * @param period billing window {@code [start, end)}
     * @return matching events (may be empty)
     */
    List<UsageEvent> findByUserAndPeriod(String userId, BillingPeriod period);

    /**
     * Returns all stored usage events across all users.
     *
     * @return all events
     */
    List<UsageEvent> findAll();

    /**
     * Removes all stored events. Intended for tests.
     */
    void clear();
}
