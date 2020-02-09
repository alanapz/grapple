package org.grapple.schema.impl;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.uncheckedCast;
import static org.jooq.lambda.Seq.seq;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.idl.SchemaPrinter;
import org.grapple.query.EntityContext;
import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityMetadataKeys;
import org.grapple.query.EntityResultType;
import org.grapple.query.QueryDefinitions;
import org.grapple.reflect.ClassLiteral;
import org.grapple.reflect.ReflectUtils;
import org.grapple.reflect.TypeConverter;
import org.grapple.reflect.TypeLiteral;
import org.grapple.scalars.GrappleScalars;
import org.grapple.schema.DefinitionImportException;
import org.grapple.schema.EntityDefaultNameGenerator;
import org.grapple.schema.EntityDefinition;
import org.grapple.schema.EntityDefinitionScannerCallback;
import org.grapple.schema.EntityQueryResolver;
import org.grapple.schema.EntityQueryScannerCallback;
import org.grapple.schema.EntitySchema;
import org.grapple.schema.EntitySchemaListener;
import org.grapple.schema.EnumTypeBuilder;
import org.grapple.schema.impl.EntityQueryScanner.QueryMethodResult;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple.utils.Utils;

final class EntitySchemaImpl implements EntitySchema {

    private final List<EntitySchemaListener> schemaListeners = new ArrayList<>();

    private final Map<Class<?>, EntityDefinitionImpl<?>> entities = new NoDuplicatesMap<>();

    private final Map<Type, GraphQLOutputType> typeMappings = new HashMap<>();

    private final Map<String, GraphQLType> customTypes = new HashMap<>();

    private EnumTypeBuilder enumTypeBuilder = EntitySchemaDefaults::buildEnumTypeForClass;

    private EntityDefaultNameGenerator entityDefaultNameGenerator = EntitySchemaDefaults.defaultNameGenerator();

    /// private static final Map<Enum<?>, GraphQLOutputType> defaultTypeMappings = new HashMap<>();

    private final TypeConverter typeConverter = new TypeConverter();

    private static final Map<Type, GraphQLOutputType> defaultTypeMappings = new HashMap<>();

    private static final SchemaPrinter defaultSchemaPrinter = new SchemaPrinter(SchemaPrinter.Options.defaultOptions()
            .includeScalarTypes(true)
            .includeExtendedScalarTypes(true)
            .includeIntrospectionTypes(false)
            .includeDirectives(false)
            .includeSchemaDefinition(true));

    @Override
    public void addSchemaListener(EntitySchemaListener schemaListener) {
        requireNonNull(schemaListener, "schemaListener");
        schemaListeners.add(schemaListener);
    }

    List<EntitySchemaListener> getSchemaListeners() {
        return unmodifiableList(schemaListeners);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> EntityDefinition<X> getEntity(Class<X> entityClass) {
        requireNonNull(entityClass, "entityClass");
        return (EntityDefinition<X>) entities.get(entityClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> EntityDefinition<X> addEntity(Class<X> entityClass) {
        requireNonNull(entityClass, "entityClass");
        if (entities.containsKey(entityClass)) {
            return (EntityDefinition<X>) entities.get(entityClass);
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
    public void importDefinitions(Set<String> packageNames, EntityDefinitionScannerCallback scannerCallback) {
        requireNonNull(packageNames, "packageNames");
        requireNonNull(scannerCallback, "scannerCallback");
        for (Class<?> definitionsClass: ReflectUtils.getAllTypesAnnotatedWith(packageNames, QueryDefinitions.class,false)) {
            if (!scannerCallback.scanDefinitions(definitionsClass)) {
                continue;
            }
            for (Field field : definitionsClass.getFields()) {
                try {
                    if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                        importDefinition(field, field.getGenericType(), field.get(null), scannerCallback);
                    }
                }
                catch (IllegalAccessException e) {
                    throw new DefinitionImportException(format("Couldn't process definitions for: %s", field), e);
                }
            }
        }
    }

    @Override
    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    @Override
    public void importQueries(Object instance, EntityQueryScannerCallback scannerCallback) {
        requireNonNull(instance, "instance");
        requireNonNull(scannerCallback, "scannerCallback");
        final Set<QueryMethodResult<?>> results = EntityQueryScanner.processClass(instance.getClass());
        if (results.isEmpty()) {
            return;
        }
        for (QueryMethodResult<?> result: results) {
            importQueryResult(instance, result, scannerCallback);
        }
    }

    @SuppressWarnings("unchecked")
    private  <X> void importQueryResult(Object instance, QueryMethodResult<X> methodResult, EntityQueryScannerCallback scannerCallback) {
        requireNonNull(instance, "instance");
        requireNonNull(scannerCallback, "scannerCallback");
        requireNonNull(methodResult, "methodResult");
        final EntityDefinitionImpl<X> entityDefinition = (EntityDefinitionImpl<X>) entities.get(methodResult.entityClass);
        if (entityDefinition == null) {
            scannerCallback.entityNotFound(methodResult.method, methodResult.entityClass);
            return;
        }
        if (!scannerCallback.acceptQuery(methodResult.method)) {
            return;
        }
        final EntityQueryResolver<X> entityQueryResolver = EntityQueryScanner.buildQueryResolver(methodResult, instance);
        final GeneratedEntityQueryDefinitionImpl<X> entityQueryDefinition = entityDefinition.addGeneratedQuery(methodResult, entityQueryResolver);
        scannerCallback.configureQuery(methodResult.method, entityQueryDefinition);
    }

    private void importDefinition(Field source, Type type, Object value, EntityDefinitionScannerCallback scannerCallback) {
        if ((value instanceof EntityField<?, ?>) || (value instanceof EntityJoin<?, ?>)) {
            final Class<?> entityClass = ReflectUtils.parseEntityFromGenericType(source, type);
            if (!entities.containsKey(entityClass) && !scannerCallback.acceptEntity(entityClass)) {
                return;
            }
            addEntity(entityClass).apply(entity -> {
                scannerCallback.configureEntity(entity);
                if (value instanceof EntityField<?, ?>) {
                    final EntityField<?, ?> entityField = (EntityField<?, ?>) value;
                    if (Boolean.TRUE.equals(entityField.getMetadata(EntityMetadataKeys.SkipImport))) {
                        return;
                    }
                    if (scannerCallback.acceptField(uncheckedCast(entity), entityField)) {
                        entity.addField(uncheckedCast(entityField)).apply(scannerCallback::configureField);
                    }
                }
                if (value instanceof EntityJoin<?, ?>) {
                    final EntityJoin<?, ?> entityJoin = (EntityJoin<?, ?>) value;
                    if (Boolean.TRUE.equals(entityJoin.getMetadata(EntityMetadataKeys.SkipImport))) {
                        return;
                    }
                    if (scannerCallback.acceptJoin(uncheckedCast(entity), entityJoin)) {
                        entity.addJoin(uncheckedCast(entityJoin)).apply(scannerCallback::configureJoin);
                    }
                }
            });
        }
        if (value instanceof Collection<?>) {
            final Type componentType = ReflectUtils.getGenericTypeArgument(type, 0);
            ((Collection<?>) value).forEach(childItem -> importDefinition(source, componentType, childItem, scannerCallback));
        }
        if (value instanceof Map<?, ?>) {
            final Type componentType = ReflectUtils.getGenericTypeArgument(type, 1); // (0 = Key, 1 = Value)
            ((Map<?, ?>) value).values().forEach(childItem -> importDefinition(source, componentType, childItem, scannerCallback));
        }
    }

    <T> FieldFilterDefinitionImpl<T> generateFieldFilter(TypeLiteral<T> fieldType, GraphQLType gqlType) {
        if (seq(schemaListeners).anyMatch(schemaListener -> !schemaListener.acceptFieldFilter(fieldType))) {
            return null;
        }
        final FieldFilterDefinitionImpl<T> fieldFilter = SimpleFieldFilterFactory.constructDefaultFilter(this, fieldType, gqlType);
        if (fieldFilter == null) {
            return null;
        }
        schemaListeners.forEach(listener -> listener.configureFieldFilter(fieldFilter));
        return fieldFilter;
    }

    @Override
    public GraphQLSchema generate() {
        final GraphQLSchema.Builder builder = GraphQLSchema.newSchema();

        final SchemaBuilderContext ctx = new SchemaBuilderContext(this);

        for (EntityDefinitionImpl<?> metadata: entities.values()) {
            metadata.build(ctx);
        }

        ctx.generate(builder);

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    <X> EntityDefinitionImpl<X> getEntityFor(EntityResultType<X> type) {
        requireNonNull(type, "type");
        return (EntityDefinitionImpl<X>) entities.get(((ClassLiteral<X>) type.getType()).getType());
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
        if (typeMappings.containsKey(type)) {
            return typeMappings.get(type);
        }
        if (defaultTypeMappings.containsKey(type)) {
            return defaultTypeMappings.get(type);
        }
        if (type instanceof Class<?>) {
            final Class<?> classType = (Class<?>) type;
            if (entities.containsKey(classType)) {
                return entities.get(classType).getEntityTypeRef();
            }
            if (classType.isArray()) {
                return GraphQLList.list(getUnwrappedTypeFor(ctx, classType.getComponentType()));
            }
        }
        if (type instanceof ParameterizedType) {
            final Class<?> classType = ReflectUtils.getRawTypeFor(type);
            if (classType != null && Collection.class.isAssignableFrom(classType)) {
                return GraphQLList.list(getUnwrappedTypeFor(ctx, ReflectUtils.getGenericTypeArgument(type, 0)));
            }
        }
        if (type instanceof GenericArrayType) {
            final GenericArrayType arrayType = (GenericArrayType) type;
            return GraphQLList.list(getUnwrappedTypeFor(ctx, arrayType.getGenericComponentType()));
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
        return defaultSchemaPrinter.print(generate());
    }

    static {

        // Primitives
        defaultTypeMappings.put(boolean.class, nonNull(Scalars.GraphQLBoolean));
        defaultTypeMappings.put(byte.class, nonNull(Scalars.GraphQLByte));
        defaultTypeMappings.put(short.class, nonNull(Scalars.GraphQLShort));
        defaultTypeMappings.put(int.class, nonNull(Scalars.GraphQLInt));
        defaultTypeMappings.put(long.class, nonNull(Scalars.GraphQLLong));
        defaultTypeMappings.put(char.class, nonNull(Scalars.GraphQLChar));
        defaultTypeMappings.put(float.class, nonNull(Scalars.GraphQLFloat));
        defaultTypeMappings.put(double.class, nonNull(Scalars.GraphQLFloat)); // No "double" type

        // Primitive object wrappers
        defaultTypeMappings.put(Boolean.class, Scalars.GraphQLBoolean);
        defaultTypeMappings.put(Byte.class, Scalars.GraphQLByte);
        defaultTypeMappings.put(Short.class, Scalars.GraphQLShort);
        defaultTypeMappings.put(Integer.class, Scalars.GraphQLInt);
        defaultTypeMappings.put(Long.class, Scalars.GraphQLLong);
        defaultTypeMappings.put(Character.class, Scalars.GraphQLChar);
        defaultTypeMappings.put(Float.class, Scalars.GraphQLFloat);
        defaultTypeMappings.put(Double.class, Scalars.GraphQLFloat); // No "Double" type

        defaultTypeMappings.put(String.class, Scalars.GraphQLString);

        defaultTypeMappings.put(UUID.class, Scalars.GraphQLID);

        defaultTypeMappings.put(BigDecimal.class, Scalars.GraphQLBigDecimal);
        defaultTypeMappings.put(BigInteger.class, Scalars.GraphQLBigInteger);

        defaultTypeMappings.put(LocalDate.class, ExtendedScalars.Date);
        defaultTypeMappings.put(OffsetDateTime.class, ExtendedScalars.DateTime);
        defaultTypeMappings.put(Object.class, ExtendedScalars.Object);
        defaultTypeMappings.put(Locale.class, ExtendedScalars.Locale);
        defaultTypeMappings.put(OffsetTime.class, ExtendedScalars.Time);
        defaultTypeMappings.put(URL.class, ExtendedScalars.Url);

        defaultTypeMappings.put(YearMonth.class, GrappleScalars.YearMonthScalar);
    }

    static {
        GraphQLObjectType.Builder builder = new GraphQLObjectType.Builder().name("FormattedTimestamp");
        builder.field(newFieldDefinition()
                .name("timestamp_utc")
                .type(nonNull(Scalars.GraphQLString)));
        builder.field(newFieldDefinition()
                .name("timestamp_local")
                .type(nonNull(Scalars.GraphQLString)));
        builder.field(newFieldDefinition()
                .name("formattedDayDateTime")
                .type(nonNull(Scalars.GraphQLString)));
        builder.field(newFieldDefinition()
                .name("epoch_seconds")
                .type(nonNull(Scalars.GraphQLLong)));

        defaultTypeMappings.put(Instant.class, builder.build());

        // defaultTypeMappings.put(EitType.class, GraphQLEnumType.newEnum().name("EitType").value("value1", "456").value("value2", "789").build());
    }
}
