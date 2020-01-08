package org.grapple.utils;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class LazyValue<T> implements Supplier<T> {

    private final Supplier<T> supplier;

    private boolean initialised;

    private T value;

    private LazyValue(Supplier<T> supplier) {
        this.supplier = requireNonNull(supplier, "supplier");
    }

    @Override
    public T get() {
        if (!this.initialised) {
            this.value = supplier.get();
            this.initialised = true;
        }
        return value;
    }

    public static <T> LazyValue<T> fixed(T value) {
        return new LazyValue<>(() -> value);
    }

    public static <T> LazyValue<T> of(Supplier<T> supplier) {
        return new LazyValue<>(supplier);
    }
}
