package org.grapple.schema.impl;

import static java.util.Objects.requireNonNull;
import static org.grapple.reflect.ReflectUtils.typeLiteral;
import static org.grapple.schema.impl.EntityQueryUtils.buildAndRegisterEntityQuery;
import static org.grapple.utils.Utils.readOnlyCopy;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Consumer;

import org.grapple.core.ElementVisibility;
import org.grapple.core.Validatable;
import org.grapple.reflect.EntityQueryMetadata.EntityQueryMethodMetadata;
import org.grapple.reflect.EntityQueryMetadata.EntityQueryParameterMetadata;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityQueryDefinitionParameter;
import org.grapple.schema.EntityQueryResolver;
import org.grapple.schema.EntityQueryType;
import org.grapple.utils.NoDuplicatesSet;

final class GeneratedEntityQueryDefinitionImpl<X> implements EntityQueryDefinitionImpl<X> {

    private final EntitySchemaImpl schema;

    private final EntityDefinitionImpl<X> entity;

    private final EntityQueryType queryType;

    private final EntityQueryResolver<X> queryResolver;

    private final Set<GeneratedParameterImpl> parameters = new NoDuplicatesSet<>();

    private String queryName;

    private String description;

    private String deprecationReason;

    private ElementVisibility visibility;

    GeneratedEntityQueryDefinitionImpl(EntitySchemaImpl schema, EntityDefinitionImpl<X> entity, EntityQueryMethodMetadata<X> methodMetadata, EntityQueryResolver<X> queryResolver) {
        requireNonNull(methodMetadata, "methodMetadata");

        this.schema = requireNonNull(schema, "schema");
        this.entity = requireNonNull(entity, "entity");
        this.queryType = requireNonNull(methodMetadata.queryType, "queryType");
        this.queryResolver = requireNonNull(queryResolver, "queryResolver");

        for (EntityQueryParameterMetadata parameterMetadata: methodMetadata.parameters.values()) {
            parameters.add(new GeneratedParameterImpl(parameterMetadata));
        }

        this.queryName = methodMetadata.queryName;
        this.description = methodMetadata.description;
        this.deprecationReason = methodMetadata.deprecationReason;
    }

    @Override
    public EntityDefinitionImpl<X> getEntity() {
        return entity;
    }

    @Override
    public EntityQueryType getQueryType() {
        return queryType;
    }

    @Override
    public void setQueryType(EntityQueryType queryType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return queryName;
    }

    @Override
    public void setName(String queryName) {
        requireNonNull(queryName, "queryName");
        this.queryName = queryName;
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
        return visibility;
    }

    @Override
    public void setVisibility(ElementVisibility visibility) {
        this.visibility = visibility;
    }

    @Override
    public Set<? extends EntityQueryDefinitionParameter<?>> getParameters() {
        return readOnlyCopy(parameters);
    }

    @Override
    public <T> EntityQueryDefinitionParameterImpl<T> addParameter(TypeLiteral<T> parameterType, Consumer<EntityQueryDefinitionParameter<T>> consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityQueryResolver<X> getQueryResolver() {
        return queryResolver;
    }

    @Override
    public void setQueryResolver(EntityQueryResolver<X> queryResolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validate() {
        if (queryName == null || queryName.trim().isEmpty()) {
            throw new IllegalArgumentException("queryName not configured");
        }
        parameters.forEach(Validatable::validate);
    }

    @Override
    public void build(SchemaBuilderContext ctx) {
        validate();
        buildAndRegisterEntityQuery(ctx, this);
    }

    private static final class GeneratedParameterImpl implements EntityQueryDefinitionParameter<Object>, Validatable {

        private final Type type;

        private final String name;

        private final int index;

        private final boolean forcedRequired;

        private boolean required;

        private String description;

        private GeneratedParameterImpl(EntityQueryParameterMetadata parameterMetadata) {
            requireNonNull(parameterMetadata, "parameterMetadata");
            this.type = requireNonNull(parameterMetadata.type, "type");
            this.name = requireNonNull(parameterMetadata.name, "name");
            this.index = parameterMetadata.index;
            this.forcedRequired = parameterMetadata.required;
            this.description = parameterMetadata.description;
        }

        @Override
        public TypeLiteral<Object> getType() {
            return typeLiteral(type);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            throw new UnsupportedOperationException();
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
            return (forcedRequired || required);
        }

        @Override
        public void setRequired(boolean required) {
            this.required = required;
        }

        @Override
        public void validate() {
            // Nothing to do here.. for the moment
        }
    }
}

