package com.billing.storage;

import com.billing.application.query.UsageQuery;
import com.billing.domain.common.CompositeKeys;
import com.billing.domain.enums.ServiceType;
import com.billing.domain.usage.UsageEvent;
import com.billing.exception.InvalidRequestException;
import com.billing.support.LogLabels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class UsageStore implements UsageRepository {

    private static final Logger log = LoggerFactory.getLogger(UsageStore.class);

    private final Map<String, CopyOnWriteArrayList<UsageEvent>> eventsByKey = new ConcurrentHashMap<>();
    private final Map<String, List<String>> keysByUser = new ConcurrentHashMap<>();
    private final Set<String> seenEventIds = ConcurrentHashMap.newKeySet();

    @Override
    public boolean save(UsageEvent event) {
        if (event == null) {
            throw new InvalidRequestException("Usage event cannot be null.");
        }

        if (event.hasEventId()) {
            if (!seenEventIds.add(event.eventId())) {
                log.debug("Skipping duplicate usage eventId={} user={} resource={} service={}",
                        event.eventId(), event.userId(), event.resourceId(), event.serviceType());
                return false;
            }
        }

        String key = storageKey(event.userId(), event.resourceId(), event.serviceType());
        log.debug("Saving usage for user={}, resource={}, service={}",
                event.userId(), event.resourceId(), event.serviceType());

        eventsByKey.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>()).add(event);
        keysByUser.computeIfAbsent(event.userId(), ignored -> new CopyOnWriteArrayList<>());
        if (!keysByUser.get(event.userId()).contains(key)) {
            keysByUser.get(event.userId()).add(key);
        }
        return true;
    }

    @Override
    public List<UsageEvent> findByQuery(UsageQuery query) {
        if (query.period() == null) {
            throw new InvalidRequestException("Billing period cannot be null.");
        }

        List<UsageEvent> candidates = query.userId() == null
                ? findAll()
                : findByUser(query.userId());

        List<UsageEvent> events = candidates.stream()
                .filter(query::matches)
                .toList();

        log.debug("Found {} events for userId={}, serviceType={}",
                events.size(), LogLabels.userId(query.userId()), LogLabels.serviceType(query.serviceType()));
        return events;
    }

    @Override
    public List<UsageEvent> findAll() {
        List<UsageEvent> all = new ArrayList<>();
        eventsByKey.values().forEach(all::addAll);
        log.debug("Loaded {} total usage events", all.size());
        return List.copyOf(all);
    }

    @Override
    public void clear() {
        eventsByKey.clear();
        keysByUser.clear();
        seenEventIds.clear();
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

    static String storageKey(String userId, String resourceId, ServiceType serviceType) {
        return CompositeKeys.join("::", userId, resourceId, serviceType.name());
    }
}
