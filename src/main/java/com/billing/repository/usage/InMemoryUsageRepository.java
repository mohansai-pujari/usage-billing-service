package com.billing.repository.usage;

import com.billing.entity.common.BillingPeriod;
import com.billing.entity.common.ResourceId;
import com.billing.entity.common.UsageKey;
import com.billing.entity.common.UserId;
import com.billing.entity.usage.UsageEvent;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryUsageRepository implements UsageRepository {

    /**
     * Primary storage.
     *
     * UsageKey -> Usage Events
     */
    private final Map<UsageKey, CopyOnWriteArrayList<UsageEvent>> usageStore = new ConcurrentHashMap<>();

    /**
     * Secondary index.
     *
     * User -> Resources used.
     */
    private final Map<UserId, Set<UsageKey>> userIndex = new ConcurrentHashMap<>();

    @Override
    public void save(UsageEvent usageEvent) {

        Objects.requireNonNull(usageEvent, "Usage event cannot be null.");

        UsageKey usageKey = UsageKey.of(usageEvent.getUserId(), usageEvent.getResourceId());

        usageStore
                .computeIfAbsent(
                        usageKey,
                        key -> new CopyOnWriteArrayList<>())
                .add(usageEvent);

        userIndex
                .computeIfAbsent(
                        usageEvent.getUserId(),
                        key -> ConcurrentHashMap.newKeySet())
                .add(usageKey);
    }

    @Override
    public List<UsageEvent> findByUser(UserId userId) {

        Objects.requireNonNull(userId);

        Set<UsageKey> usageKeys =
                userIndex.get(userId);

        if (usageKeys == null || usageKeys.isEmpty()) {
            return List.of();
        }

        List<UsageEvent> result = new ArrayList<>();

        for (UsageKey usageKey : usageKeys) {

            List<UsageEvent> events =
                    usageStore.get(usageKey);

            if (events != null) {
                result.addAll(events);
            }
        }

        return List.copyOf(result);
    }

    @Override
    public List<UsageEvent> findByUserAndPeriod(
            UserId userId,
            BillingPeriod billingPeriod) {

        Objects.requireNonNull(userId);
        Objects.requireNonNull(billingPeriod);

        return findByUser(userId)
                .stream()
                .filter(event ->
                        billingPeriod.contains(
                                event.getTimestamp()))
                .toList();
    }

    @Override
    public List<UsageEvent> findByUserAndResource(
            UserId userId,
            ResourceId resourceId) {

        Objects.requireNonNull(userId);
        Objects.requireNonNull(resourceId);

        UsageKey usageKey =
                UsageKey.of(
                        userId,
                        resourceId);

        List<UsageEvent> events =
                usageStore.get(usageKey);

        if (events == null) {
            return List.of();
        }

        return List.copyOf(events);
    }

    @Override
    public List<UsageEvent> findAll() {

        List<UsageEvent> result = new ArrayList<>();

        usageStore.values()
                .forEach(result::addAll);

        return List.copyOf(result);
    }

    /**
     * Returns true if repository contains any usage
     * for the supplied user.
     */
    public boolean exists(UserId userId) {

        return userIndex.containsKey(userId);
    }

    /**
     * Removes all stored usage.
     * Useful for integration tests.
     */
    public void clear() {

        usageStore.clear();
        userIndex.clear();
    }

    /**
     * Number of user-resource pairs stored.
     */
    public int usageKeyCount() {

        return usageStore.size();
    }

    /**
     * Total number of users.
     */
    public int userCount() {

        return userIndex.size();
    }

    /**
     * Total number of events.
     */
    public int eventCount() {

        return usageStore.values()
                .stream()
                .mapToInt(List::size)
                .sum();
    }
}