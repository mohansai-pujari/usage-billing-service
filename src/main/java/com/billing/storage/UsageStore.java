package com.billing.storage;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.usage.UsageEvent;
import com.billing.exception.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory implementation of {@link UsageRepository}.
 * <p>
 * Responsibility: Stores and retrieves usage events keyed by user and resource, with a secondary
 * index by user for efficient period queries.
 * <p>
 * Design patterns: Repository — concrete in-memory implementation; thread-safe collections for
 * concurrent access.
 */
@Repository
public class UsageStore implements UsageRepository {

    private static final Logger log = LoggerFactory.getLogger(UsageStore.class);

    /** Primary store: composite key (user::resource) → list of events. */
    private final Map<String, CopyOnWriteArrayList<UsageEvent>> eventsByKey = new ConcurrentHashMap<>();

    /** Secondary index: userId → list of composite keys for that user. */
    private final Map<String, List<String>> keysByUser = new ConcurrentHashMap<>();

    @Override
    public void save(UsageEvent event) {
        if (event == null) {
            throw new InvalidRequestException("Usage event cannot be null.");
        }

        String key = storageKey(event.userId(), event.resourceId());
        log.debug(
                "Saving usage event for user={}, resource={}, service={}, quantity={}",
                event.userId(),
                event.resourceId(),
                event.serviceType(),
                event.quantity());

        eventsByKey.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>()).add(event);
        keysByUser.computeIfAbsent(event.userId(), ignored -> new CopyOnWriteArrayList<>());
        if (!keysByUser.get(event.userId()).contains(key)) {
            keysByUser.get(event.userId()).add(key);
        }
    }

    @Override
    public List<UsageEvent> findByUserAndPeriod(String userId, BillingPeriod period) {
        requireUserId(userId);
        requirePeriod(period);

        List<UsageEvent> events = findByUser(userId).stream()
                .filter(event -> period.contains(event.timestamp()))
                .toList();

        log.debug("Found {} usage events for user={} in period [{}, {})", events.size(), userId, period.start(), period.end());
        return events;
    }

    @Override
    public List<UsageEvent> findAll() {
        List<UsageEvent> all = new ArrayList<>();
        eventsByKey.values().forEach(all::addAll);
        log.debug("Loaded {} total usage events from store", all.size());
        return List.copyOf(all);
    }

    @Override
    public void clear() {
        log.debug("Clearing all usage events from store");
        eventsByKey.clear();
        keysByUser.clear();
    }

    private List<UsageEvent> findByUser(String userId) {
        List<String> keys = keysByUser.get(userId);
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        List<UsageEvent> events = new ArrayList<>();
        for (String key : keys) {
            List<UsageEvent> stored = eventsByKey.get(key);
            if (stored != null) {
                events.addAll(stored);
            }
        }
        return List.copyOf(events);
    }

    private static void requireUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new InvalidRequestException("User id is required.");
        }
    }

    private static void requirePeriod(BillingPeriod period) {
        if (period == null) {
            throw new InvalidRequestException("Billing period cannot be null.");
        }
    }

    private static String storageKey(String userId, String resourceId) {
        return userId + "::" + resourceId;
    }
}
