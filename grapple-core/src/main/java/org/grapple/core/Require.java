package org.grapple.core;

import org.jetbrains.annotations.NotNull;

public final class Require {

    private Require() {

    }

    public static <T> T requireNonNull(@NotNull T obj, @NotNull String message) {
        //noinspection ConstantConditions
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

    public static <T> T requireNonNullArgument(@NotNull T obj, @NotNull String message) {
        //noinspection ConstantConditions
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }
}
