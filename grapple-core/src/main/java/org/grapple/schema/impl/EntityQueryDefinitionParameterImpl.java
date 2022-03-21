package org.grapple.schema.impl;

import static java.util.Objects.requireNonNull;

import org.grapple.core.Validatable;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityQueryDefinitionParameter;

final class EntityQueryDefinitionParameterImpl<T> implements EntityQueryDefinitionParameter<T>, Validatable {

    private final TypeLiteral<T> type;

    private String name;

    private String description;

    private boolean required;

    EntityQueryDefinitionParameterImpl(TypeLiteral<T> type) {
        this.type = requireNonNull(type, "type");
    }

    @Override
    public TypeLiteral<T> getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        requireNonNull(name, "name");
        this.name = name;
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
    public boolean isRequired() {
        return required;
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name not configured");
        }
    }
}
