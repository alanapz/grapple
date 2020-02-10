package org.grapple.schema.impl;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.readOnlyCopy;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.grapple.core.ElementVisibility;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.FieldFilterDefinition;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple.utils.Utils;

final class FieldFilterDefinitionImpl<T> implements FieldFilterDefinition<T> {

    private final EntitySchemaImpl schema;

    private final TypeLiteral<T> fieldType;

    private String entityName;

    private String description;

    private String deprecationReason;

    private final Map<String, SimpleFieldFilterItem<T>> items = new NoDuplicatesMap<>();

    FieldFilterDefinitionImpl(EntitySchemaImpl schema, TypeLiteral<T> fieldType) {
        this.schema = requireNonNull(schema, "schema");
        this.fieldType = requireNonNull(fieldType, "fieldType");
        this.entityName = requireNonNull(schema.getEntityDefaultNameGenerator().generateFieldFilterEntityName(fieldType), "entityName");
    }

    @Override
    public TypeLiteral<T> getFieldType() {
        return fieldType;
    }

    @Override
    public String getName() {
        return entityName;
    }

    @Override
    public void setName(String entityName) {
        requireNonNull(entityName, "entityName");
        this.entityName = entityName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDeprecationReason() {
        return deprecationReason;
    }

    @Override
    public void setDeprecationReason(String deprecationReason) {
        this.deprecationReason = deprecationReason;
    }

    @Override
    public ElementVisibility getVisibility() {
        return null;
    }

    @Override
    public void setVisibility(ElementVisibility visibility) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, SimpleFieldFilterItem<T>> getItems() {
        return readOnlyCopy(items);
    }

    @Override
    public FieldFilterDefinition<T> addItem(SimpleFieldFilterItem<T> item) {
        requireNonNull(item, "item");
        items.put(item.name, item);
        return this;
    }

    @Override
    public String toString() {
        return format("%s[%s]", entityName, fieldType);
    }

    @Override
    public FieldFilterDefinition<T> apply(Consumer<FieldFilterDefinition<T>> consumer) {
        return Utils.apply(this, consumer);
    }

    @Override
    public <Z> Z invoke(Function<FieldFilterDefinition<T>, Z> function) {
        return requireNonNull(function, "function").apply(this);
    }
}
