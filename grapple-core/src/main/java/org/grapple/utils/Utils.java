package org.grapple.utils;

import java.util.function.Consumer;
import org.reflections.Reflections;

public final class Utils {

    private Utils() {

    }

    public static void main(String[] args) {
        Reflections reflections = new Reflections("org.grapple");
        System.out.println(reflections.getSubTypesOf(Chainable.class));
    }

    public static <T> Consumer<T> nullConsumer() {
        return (val) -> {};
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
