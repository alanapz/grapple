package org.grapple.core;

public interface MetadataAware {

    <M> M getMetadata(MetadataKey<M> metadataKey);

}
