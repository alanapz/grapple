package org.grapple.core;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public class MetadataKey<M> {

    private final String name;

    public MetadataKey(String name) {
        this.name = requireNonNull(name, "name");
    }

    @Override
    public String toString() {
        return name;
    }
}
