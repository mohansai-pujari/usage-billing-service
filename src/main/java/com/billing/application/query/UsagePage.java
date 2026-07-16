package com.billing.application.query;

import com.billing.exception.InvalidRequestException;

import java.util.List;

public record UsagePage<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public UsagePage {
        content = List.copyOf(content);
    }

    public static <T> UsagePage<T> unpaged(List<T> items) {
        int total = items.size();
        return new UsagePage<>(items, 0, total, total, total == 0 ? 0 : 1);
    }

    public static <T> UsagePage<T> of(List<T> items, Integer page, Integer size) {
        if (page == null && size == null) {
            return unpaged(items);
        }

        int effectivePage = page != null ? page : DEFAULT_PAGE;
        int effectiveSize = size != null ? size : DEFAULT_SIZE;
        validatePagination(effectivePage, effectiveSize);

        int total = items.size();
        if (total == 0) {
            return new UsagePage<>(List.of(), effectivePage, effectiveSize, 0, 0);
        }

        int totalPages = (int) Math.ceil((double) total / effectiveSize);
        int fromIndex = Math.min(effectivePage * effectiveSize, total);
        if (fromIndex >= total) {
            return new UsagePage<>(List.of(), effectivePage, effectiveSize, total, totalPages);
        }

        int toIndex = Math.min(fromIndex + effectiveSize, total);
        return new UsagePage<>(items.subList(fromIndex, toIndex), effectivePage, effectiveSize, total, totalPages);
    }

    private static void validatePagination(int page, int size) {
        if (page < 0) {
            throw new InvalidRequestException("Page must be zero or greater.");
        }
        if (size <= 0) {
            throw new InvalidRequestException("Size must be greater than zero.");
        }
        if (size > MAX_SIZE) {
            throw new InvalidRequestException("Size must not exceed " + MAX_SIZE + ".");
        }
    }
}
