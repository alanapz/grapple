package org.grapple.schema;

import org.grapple.core.Chainable;
import org.grapple.core.ElementVisibility;

public final class EntitySchemaScannerFilterConfiguration<X, T> implements Chainable<EntitySchemaScannerFilterConfiguration<X, T>> {

    private String name;

    private String description;

    private String deprecationReason;

    private ElementVisibility visibility;

    private EntityFilterItemResolver<X, T> filterResolver;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeprecationReason() {
        return deprecationReason;
    }

    public void setDeprecationReason(String deprecationReason) {
        this.deprecationReason = deprecationReason;
    }

    public ElementVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ElementVisibility visibility) {
        this.visibility = visibility;
    }

    public EntityFilterItemResolver<X, T> getFilterResolver() {
        return filterResolver;
    }

    public void setFilterResolver(EntityFilterItemResolver<X, T> filterResolver) {
        this.filterResolver = filterResolver;
    }
}
