package org.grapple.schema.impl;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static org.grapple.reflect.ReflectUtils.typeLiteral;
import static org.grapple.schema.impl.EntityQueryUtils.buildAndRegisterEntityQuery;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.grapple.core.Validatable;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityQueryDefinition;
import org.grapple.schema.EntityQueryDefinitionParameter;
import org.grapple.schema.EntityQueryResolver;
import org.grapple.schema.EntityQueryType;
import org.grapple.schema.impl.EntityQueryScanner.QueryMethodResult;
import org.grapple.utils.NoDuplicatesSet;
import org.grapple.utils.Utils;

final class GeneratedEntityQueryDefinitionImpl<X> implements EntityQueryDefinitionImpl<X> {

    private final EntitySchemaImpl schema;

    private final EntityDefinitionImpl<X> entity;

    private final EntityQueryType queryType;

    private final EntityQueryResolver<X> queryResolver;

    private final Set<GeneratedParameterImpl> parameters = new NoDuplicatesSet<>();

    private String queryName;

    private String description;

    private String deprecationReason;

    GeneratedEntityQueryDefinitionImpl(EntitySchemaImpl schema, EntityDefinitionImpl<X> entity, QueryMethodResult<X> methodResult, EntityQueryResolver<X> queryResolver) {
        requireNonNull(methodResult, "methodResult");

        this.schema = requireNonNull(schema, "schema");
        this.entity = requireNonNull(entity, "entity");
        this.queryType = requireNonNull(methodResult.queryType, "queryType");
        this.queryResolver = requireNonNull(queryResolver, "queryResolver");

        for (EntityQueryScanner.QueryParameterResult parameterResult: methodResult.parameters.values()) {
            parameters.add(new GeneratedParameterImpl(parameterResult));
        }

        this.queryName = methodResult.queryName;
        this.description = methodResult.description;
        this.deprecationReason = methodResult.deprecationReason;
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
    public String getQueryName() {
        return queryName;
    }

    @Override
    public void setQueryName(String queryName) {
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
    public Set<? extends EntityQueryDefinitionParameter<?>> getParameters() {
        return unmodifiableSet(parameters);
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

    @Override
    public GeneratedEntityQueryDefinitionImpl<X> apply(Consumer<EntityQueryDefinition<X>> consumer) {
        return Utils.apply(this, consumer);
    }

    @Override
    public <Z> Z invoke(Function<EntityQueryDefinition<X>, Z> function) {
        return requireNonNull(function, "function").apply(this);
    }

    private static final class GeneratedParameterImpl implements EntityQueryDefinitionParameter<Object>, Validatable {

        private final Type type;

        private final String name;

        private final int index;

        private final boolean required;

        private String description;

        private GeneratedParameterImpl(EntityQueryScanner.QueryParameterResult parameterResult) {
            requireNonNull(parameterResult, "parameterResult");

            this.type = requireNonNull(parameterResult.type, "type");
            this.name = requireNonNull(parameterResult.name, "name");
            this.index = parameterResult.index;
            this.required = parameterResult.required;

            this.description = parameterResult.description;
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
            return required;
        }

        @Override
        public void setRequired(boolean required) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTypeAlias() {
            return null;
        }

        @Override
        public void setTypeAlias(String typeAlias) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void validate() {
            // Nothing to do here.. for the moment
        }

        @Override
        public EntityQueryDefinitionParameter<Object> apply(Consumer<EntityQueryDefinitionParameter<Object>> consumer) {
            return Utils.apply(this, consumer);
        }

        @Override
        public <Z> Z invoke(Function<EntityQueryDefinitionParameter<Object>, Z> function) {
            return requireNonNull(function, "function").apply(this);
        }

    }
}

