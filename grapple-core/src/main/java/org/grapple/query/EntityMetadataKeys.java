package org.grapple.query;

import org.grapple.core.ElementVisibility;
import org.grapple.core.MetadataKey;

public final class EntityMetadataKeys {

    public static final MetadataKey<Boolean> FieldNotExported = new MetadataKey<>("fieldNotExported");

    public static final MetadataKey<String> Description = new MetadataKey<>("description");

    public static final MetadataKey<String> DeprecationReason = new MetadataKey<>("deprecationReason");

    public static final MetadataKey<ElementVisibility> Visibility = new MetadataKey<>("visibility");

    private EntityMetadataKeys() {

    }

}
