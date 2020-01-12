package org.grapple.query;

import org.grapple.core.MetadataKey;

public final class EntityMetadataKeys {

    public static final MetadataKey<String> DESCRIPTION = new MetadataKey<>();

    public static final MetadataKey<Boolean> IS_DEPRECATED = new MetadataKey<>();

    public static final MetadataKey<String> DEPRECATION_REASON = new MetadataKey<>();

    private EntityMetadataKeys() {

    }

}
