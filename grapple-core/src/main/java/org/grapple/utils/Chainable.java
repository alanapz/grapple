package org.grapple.utils;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Chainable<X> {

    X apply(Consumer<X> consumer);

    <Z> Z invoke(Function<X, Z> function);

}
