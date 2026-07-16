package com.billing.domain.common;

public final class CompositeKeys {

    private CompositeKeys() {
    }

    public static String join(String delimiter, Object... parts) {
        StringBuilder key = new StringBuilder();
        for (int index = 0; index < parts.length; index++) {
            if (index > 0) {
                key.append(delimiter);
            }
            key.append(parts[index]);
        }
        return key.toString();
    }
}
