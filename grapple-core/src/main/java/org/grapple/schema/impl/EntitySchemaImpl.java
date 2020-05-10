package org.grapple.schema.impl;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static org.jooq.lambda.Seq.seq;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.idl.SchemaPrinter;
import org.grapple.query.EntityResultType;
import org.grapple.reflect.ReflectUtils;
import org.grapple.reflect.TypeConverter;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityDefaultNameGenerator;
import org.grapple.schema.EntityQueryExecutionListener;
import org.grapple.schema.EntitySchema;
import org.grapple.schema.EntitySchemaListener;
import org.grapple.schema.EntitySchemaScannerCallback;
import org.grapple.schema.EnumTypeBuilder;
import org.grapple.schema.UnmanagedQueryDefinition;
import org.grapple.schema.UnmanagedTypeDefinition;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple.utils.Utils;

final class EntitySchemaImpl implements EntitySchema {

    private final List<EntitySchemaListener> schemaListeners = new ArrayList<>();

    private final EntityQueryExecutionListeners entityQueryExecutionListeners = new EntityQueryExecutionListeners();

    private final Map<Class<?>, EntityDefinitionImpl<?>> entities = new NoDuplicatesMap<>();

    private EnumTypeBuilder enumTypeBuilder = EntitySchemaDefaults::buildEnumTypeForClass;

    private EntityDefaultNameGenerator entityDefaultNameGenerator = EntitySchemaDefaults.defaultNameGenerator();

    private final TypeConverter typeConverter = new TypeConverter();

    private final Map<String, UnmanagedQueryDefinitionImpl> unmanagedQueries = new NoDuplicatesMap<>();

    private final Map<Type, UnmanagedTypeDefinitionImpl<?>> unmanagedTypes = new NoDuplicatesMap<>(new LinkedHashMap<>());

    private static final Map<Class<?>, GraphQLInputType> inputTypeMappings = new HashMap<>();

    private static final Map<Class<?>, GraphQLOutputType> outputTypeMappings = new HashMap<>();

    public ZoneId getTimeZone() {
        return ZoneId.systemDefault();
    }

    private static final SchemaPrinter defaultSchemaPrinter = new SchemaPrinter(SchemaPrinter.Options.defaultOptions()
            .includeScalarTypes(true)
            .includeExtendedScalarTypes(true)
            .includeIntrospectionTypes(false)
            .includeDirectives(false)
            .includeSchemaDefinition(true));

    EntitySchemaImpl() {
        EntitySchemaDefaults.addDefaultTypes(this);
    }

    @Override
    public void addSchemaListener(EntitySchemaListener schemaListener) {
        requireNonNull(schemaListener, "schemaListener");
        schemaListeners.add(schemaListener);
    }

    List<EntitySchemaListener> getSchemaListeners() {
        return unmodifiableList(schemaListeners);
    }

    @Override
    public void addEntityQueryExecutionListener(EntityQueryExecutionListener listener) {
        requireNonNull(listener, "listener");
        entityQueryExecutionListeners.addListener(listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> EntityDefinitionImpl<X> getEntity(Class<X> entityClass) {
        requireNonNull(entityClass, "entityClass");
        return (EntityDefinitionImpl<X>) entities.get(entityClass);
    }

    @Override
    public <X> EntityDefinitionImpl<X> addEntity(Class<X> entityClass) {
        requireNonNull(entityClass, "entityClass");
        final @SuppressWarnings("unchecked") EntityDefinitionImpl<X> existing = (EntityDefinitionImpl<X>) entities.get(entityClass);
        if (existing != null) {
            return existing;
        }
        final EntityDefinitionImpl<X> entityDefinition = new EntityDefinitionImpl<>(this, entityClass);
        entities.put(entityClass, entityDefinition);
        return entityDefinition;
    }

    @Override
    public EnumTypeBuilder getEnumTypeBuilder() {
        return enumTypeBuilder;
    }

    @Override
    public void setEnumTypeBuilder(EnumTypeBuilder enumTypeBuilder) {
        requireNonNull(enumTypeBuilder, "enumTypeBuilder");
        this.enumTypeBuilder = enumTypeBuilder;
    }

    @Override
    public EntityDefaultNameGenerator getEntityDefaultNameGenerator() {
        return entityDefaultNameGenerator;
    }

    @Override
    public void setEntityDefaultNameGenerator(EntityDefaultNameGenerator entityDefaultNameGenerator) {
        requireNonNull(entityDefaultNameGenerator, "entityDefaultNameGenerator");
        this.entityDefaultNameGenerator = entityDefaultNameGenerator;
    }

    @Override
    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    @Override
    public void addUnmanagedQuery(String queryAlias, Consumer<UnmanagedQueryDefinition> consumer) {
        requireNonNull(queryAlias, "queryAlias");
        requireNonNull(consumer, "consumer");
        final UnmanagedQueryDefinitionImpl existing = unmanagedQueries.get(queryAlias);
        if (existing != null) {
            consumer.accept(existing);
            return;
        }
        final UnmanagedQueryDefinitionImpl unmanagedQuery = new UnmanagedQueryDefinitionImpl(queryAlias);
        consumer.accept(unmanagedQuery);
        unmanagedQuery.validate();
        unmanagedQueries.put(queryAlias, unmanagedQuery);
    }

    @Override
    public <T> void addUnmanagedType(TypeLiteral<T> type, Consumer<UnmanagedTypeDefinition<T>> consumer) {
        requireNonNull(type, "type");
        requireNonNull(consumer, "consumer");
        final @SuppressWarnings("unchecked") UnmanagedTypeDefinitionImpl<T> existing = (UnmanagedTypeDefinitionImpl<T>) unmanagedTypes.get(type.getType());
        if (existing != null) {
            consumer.accept(existing);
            return;
        }
        final UnmanagedTypeDefinitionImpl<T> unmanagedType = new UnmanagedTypeDefinitionImpl<>(type);
        consumer.accept(unmanagedType);
        unmanagedType.validate();
        unmanagedTypes.put(type.getType(), unmanagedType);
    }

    @Override
    public EntitySchemaScannerImpl buildEntitySchemaScanner(EntitySchemaScannerCallback scannerCallback) {
        requireNonNull(scannerCallback, "scannerCallback");
        return new EntitySchemaScannerImpl(this, scannerCallback);
    }

    <T> FieldFilterDefinitionImpl<T> generateFieldFilter(TypeLiteral<T> fieldType, GraphQLInputType inputType) {
        if (inputType == null) {
            return null; /// XXX: TODO: FIXME
        }
        if (seq(schemaListeners).anyMatch(schemaListener -> !schemaListener.acceptFieldFilter(fieldType))) {
            return null;
        }
        final FieldFilterDefinitionImpl<T> fieldFilter = SimpleFieldFilterFactory.constructDefaultFilter(this, fieldType, inputType);
        if (fieldFilter == null) {
            return null;
        }
        schemaListeners.forEach(listener -> listener.configureFieldFilter(fieldFilter));
        return fieldFilter;
    }

    @Override
    public EntitySchemaResultImpl generate() {
        final GraphQLSchema.Builder builder = GraphQLSchema.newSchema();

        final SchemaBuilderContext ctx = new SchemaBuilderContext(this,
                null, // XXX: TODO: FIXME Move field visibility here instead GQP visiiblity
                enumTypeBuilder,
                typeConverter,
                entityQueryExecutionListeners.copy(),
                "Query");

        for (UnmanagedTypeDefinitionImpl<?> unmanagedType: unmanagedTypes.values()) {
            unmanagedType.build(ctx);
        }
        for (UnmanagedQueryDefinitionImpl unmanagedQuery: unmanagedQueries.values()) {
            unmanagedQuery.build(ctx);
        }

        for (EntityDefinitionImpl<?> metadata: entities.values()) {
            metadata.build(ctx);
        }

        ctx.generate(builder);

        return new EntitySchemaResultImpl(
                builder.build(),
                ctx.getSchemaBuilderElementVisibility());
    }

    @SuppressWarnings("unchecked")
    <X> EntityDefinitionImpl<X> getEntityFor(EntityResultType<X> resultType) {
        requireNonNull(resultType, "resultType");
        final Type type = resultType.getType().getType();
        if (!(type instanceof Class<?>)) { // Only concrete classes can be entities
            return null;
        }
        return (EntityDefinitionImpl<X>) entities.get(type);
    }

    GraphQLOutputType getResultTypeFor(SchemaBuilderContext ctx, EntityResultType<?> resultType) {
        requireNonNull(resultType, "resultType");
        final GraphQLOutputType unwrappedType = getUnwrappedTypeFor(ctx, resultType.getType().getType());
        if (unwrappedType == null) {
            return null;
        }
        return SchemaUtils.wrapNonNullIfNecessary(resultType, unwrappedType);
    }

    public GraphQLOutputType getUnwrappedTypeFor(SchemaBuilderContext ctx, Type type) {
        final GraphQLOutputType defaultType = DefaultTypeMappings.getDefaultOutputTypeFor(type);
        if (defaultType != null) {
            return defaultType;
        }
        if (type instanceof Class<?>) {
            final Class<?> classType = (Class<?>) type;
            if (entities.containsKey(classType)) {
                return entities.get(classType).getEntityTypeRef();
            }
            if (classType.isArray()) {
                final GraphQLType unwrappedType = getUnwrappedTypeFor(ctx, classType.getComponentType());
                return (unwrappedType != null ? GraphQLList.list(unwrappedType) : null);
            }
        }
        if (type instanceof ParameterizedType) {
            final Class<?> classType = ReflectUtils.getRawTypeFor(type);
            if (classType != null && Collection.class.isAssignableFrom(classType)) {
                final GraphQLType unwrappedType = getUnwrappedTypeFor(ctx, ReflectUtils.getGenericTypeArgument(type, 0));
                return (unwrappedType != null ? GraphQLList.list(unwrappedType) : null);
            }
        }
        if (type instanceof GenericArrayType) {
            final GraphQLType unwrappedType = getUnwrappedTypeFor(ctx, ((GenericArrayType) type).getGenericComponentType());
            return (unwrappedType != null ? GraphQLList.list(unwrappedType) : null);
        }
        return ctx.getUnwrappedTypeFor(type);
    }

    @Override
    public EntitySchema apply(Consumer<EntitySchema> consumer) {
        return Utils.apply(this, consumer);
    }

    @Override
    public <Z> Z invoke(Function<EntitySchema, Z> function) {
        return requireNonNull(function, "function").apply(this);
    }

    @Override
    public String toString() {
        return defaultSchemaPrinter.print(generate().getSchema());
    }

}
