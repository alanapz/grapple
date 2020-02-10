package org.grapple.schema.impl;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static org.grapple.schema.impl.EntityQueryUtils.buildAndRegisterEntityQuery;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.grapple.core.Validatable;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityQueryDefinition;
import org.grapple.schema.EntityQueryDefinitionParameter;
import org.grapple.schema.EntityQueryResolver;
import org.grapple.schema.EntityQueryType;
import org.grapple.utils.NoDuplicatesSet;
import org.grapple.utils.Utils;

final class UserEntityQueryDefinitionImpl<X> implements EntityQueryDefinitionImpl<X> {

    private final EntitySchemaImpl schema;

    private final EntityDefinitionImpl<X> entity;

    private EntityQueryType queryType = EntityQueryType.LIST;

    private String queryName;

    private String description;

    private String deprecationReason;

    private final Set<EntityQueryDefinitionParameterImpl<?>> parameters = new NoDuplicatesSet<>();

    private EntityQueryResolver<X> queryResolver;

    UserEntityQueryDefinitionImpl(EntitySchemaImpl schema, EntityDefinitionImpl<X> entity) {
        this.schema = requireNonNull(schema, "schema");
        this.entity = requireNonNull(entity, "entity");
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
        requireNonNull(queryType, "queryType");
        this.queryType = queryType;
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
        requireNonNull(parameterType, "parameterType");
        requireNonNull(consumer, "consumer");
        final EntityQueryDefinitionParameterImpl<T> parameter = Utils.applyAndValidate(new EntityQueryDefinitionParameterImpl<>(parameterType), consumer);
        parameters.add(parameter);
        return parameter;
    }

    @Override
    public EntityQueryResolver<X> getQueryResolver() {
        return queryResolver;
    }

    @Override
    public void setQueryResolver(EntityQueryResolver<X> queryResolver) {
        requireNonNull(queryResolver, "queryResolver");
        this.queryResolver = queryResolver;
    }

    @Override
    public void validate() {
        if (queryType == null) {
            throw new IllegalArgumentException("queryType not configured");
        }
        if (queryName == null || queryName.trim().isEmpty()) {
            throw new IllegalArgumentException("queryName not configured");
        }
        if (queryResolver == null) {
            throw new IllegalArgumentException("queryResolver not configured");
        }
        parameters.forEach(Validatable::validate);
    }

    @Override
    public void build(SchemaBuilderContext ctx) {
        validate();
        buildAndRegisterEntityQuery(ctx, this);
    }

    @Override
    public UserEntityQueryDefinitionImpl<X> apply(Consumer<EntityQueryDefinition<X>> consumer) {
        return Utils.apply(this, consumer);
    }

    @Override
    public <Z> Z invoke(Function<EntityQueryDefinition<X>, Z> function) {
        return requireNonNull(function, "function").apply(this);
    }
}

