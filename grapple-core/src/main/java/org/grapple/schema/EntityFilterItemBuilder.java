package org.grapple.schema;

import org.grapple.core.Chainable;
import org.grapple.core.ElementVisibility;

public final class EntityFilterItemBuilder<X, T> implements Chainable<EntityFilterItemBuilder<X, T>> {

    private String name;

    private String description;

    private String deprecationReason;

    private ElementVisibility visibility;

    private EntityFilterItemResolver<X, T> filterResolver;

    public String getName() {
        return name;
    }

    public EntityFilterItemBuilder<X, T> setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public EntityFilterItemBuilder<X, T> setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDeprecationReason() {
        return deprecationReason;
    }

    public EntityFilterItemBuilder<X, T> setDeprecationReason(String deprecationReason) {
        this.deprecationReason = deprecationReason;
        return this;
    }

    public ElementVisibility getVisibility() {
        return visibility;
    }

    public EntityFilterItemBuilder<X, T> setVisibility(ElementVisibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public EntityFilterItemResolver<X, T> getFilterResolver() {
        return filterResolver;
    }

    public EntityFilterItemBuilder<X, T> setFilterResolver(EntityFilterItemResolver<X, T> filterResolver) {
        this.filterResolver = filterResolver;
        return this;
    }
}
