package org.grapple.core;

import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public interface Chainable<X> {

    default X apply(Consumer<X> consumer) {
        if (consumer != null) {
            consumer.accept((X) this);
        }
        return (X) this;
    }

    default <Z> Z invoke(@NotNull Function<X, Z> function) {
        return function.apply((X) this);
    }
}
