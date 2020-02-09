package org.grapple.query;

import org.grapple.core.MetadataKey;

public final class EntityMetadataKeys {

    public static final MetadataKey<Boolean> SkipImport = new MetadataKey<>("skipImport");

    public static final MetadataKey<String> Description = new MetadataKey<>("description");

    public static final MetadataKey<String> DeprecationReason = new MetadataKey<>("deprecationReason");

    public static final MetadataKey<EntitySchemaVisibility> Visibility = new MetadataKey<>("visibility");

    private EntityMetadataKeys() {

    }

}
