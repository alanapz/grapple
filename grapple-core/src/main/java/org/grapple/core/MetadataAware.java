package org.grapple.core;

public interface MetadataAware {

    Object getMetadataValue(MetadataKey<?> metadataKey);

    @SuppressWarnings("unchecked")
    default <M> M getMetadata(MetadataKey<M> metadataKey) {
        return (M) getMetadataValue(metadataKey);
    }
}
