package org.grapple.query;

import static java.util.Objects.requireNonNull;

public final class QueryParameter<T> {

    private final String name;

    public QueryParameter(String name) {
        this.name = requireNonNull(name, "name");
    }

    @Override
    public String toString() {
        return name;
    }
}
