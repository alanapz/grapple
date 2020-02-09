package org.grapple.utils;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.grapple.core.MetadataKey;

public final class MetadataValues {

    private final Map<MetadataKey<?>, Supplier<?>> values = new HashMap<>();

    private final Map<MetadataKey<?>, Object> defaultValues = new HashMap<>();

    public <M> void put(MetadataKey<M> metadataKey, M value) {
        requireNonNull(metadataKey, "metadataKey");
        values.put(metadataKey, () -> value);
    }

    public <M> void put(MetadataKey<M> metadataKey, Supplier<M> supplier) {
        requireNonNull(metadataKey, "metadataKey");
        requireNonNull(supplier, "supplier");
        values.put(metadataKey, supplier);
    }

    public <M> void putDefault(MetadataKey<M> metadataKey, M value) {
        requireNonNull(metadataKey, "metadataKey");
        defaultValues.put(metadataKey, value);
    }

    @SuppressWarnings("unchecked")
    public <M> M get(MetadataKey<M> metadataKey) {
        requireNonNull(metadataKey, "key");
        final Supplier<M> supplier = (Supplier<M>) values.get(metadataKey);
        if (supplier != null) {
            return supplier.get();
        }
        return (M) defaultValues.get(metadataKey);
    }
}
