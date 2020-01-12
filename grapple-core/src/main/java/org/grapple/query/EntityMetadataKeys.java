package org.grapple.query;

import org.grapple.core.MetadataKey;

public final class EntityMetadataKeys {

    public static final MetadataKey<String> Description = new MetadataKey<>();

    public static final MetadataKey<Boolean> IsDeprecated = new MetadataKey<>();

    public static final MetadataKey<String> DeprecationReason = new MetadataKey<>();

    public static final MetadataKey<Boolean> IsLongRunning = new MetadataKey<>();

    private EntityMetadataKeys() {

    }

}
