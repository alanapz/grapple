package org.grapple.utils;

public final class Utils {

    private Utils() {

    }

    @SafeVarargs
    public static <T> T coalesce(T... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        for (T value: values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public static String stringCoalesce(String... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        for (String value: values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }
}
