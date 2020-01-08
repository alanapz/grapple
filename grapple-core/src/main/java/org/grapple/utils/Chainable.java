package org.grapple.utils;

import java.util.function.Consumer;

public interface Chainable<X> {

    X apply(Consumer<X> consumer);

}
