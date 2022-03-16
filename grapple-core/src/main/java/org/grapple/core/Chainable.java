package org.grapple.core;

import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public interface Chainable<X> {

    @SuppressWarnings("unchecked")
    default X apply(Consumer<X> consumer) {
        if (consumer != null) {
            consumer.accept((X) this);
        }
        return (X) this;
    }

    @SuppressWarnings("unchecked")
    default <Z> Z invoke(@NotNull Function<X, Z> function) {
        return function.apply((X) this);
    }
}
