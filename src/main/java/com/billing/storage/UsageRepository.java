package com.billing.storage;

import com.billing.application.query.UsageQuery;
import com.billing.domain.usage.UsageEvent;

import java.util.List;

public interface UsageRepository {

    /**
     * Persists a usage event. Returns {@code false} when an event with the same {@code eventId} was already stored.
     */
    boolean save(UsageEvent event);

    List<UsageEvent> findByQuery(UsageQuery query);

    List<UsageEvent> findAll();

    void clear();
}
