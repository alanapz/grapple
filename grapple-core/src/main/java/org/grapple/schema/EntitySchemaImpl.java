package org.grapple.schema;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.idl.SchemaPrinter;
import org.grapple.query.EntityResultType;
import org.grapple.scalars.GrappleScalars;

final class EntitySchemaImpl implements EntitySchema {

    private final Map<Class<?>, EntityDefinitionImpl<?>> entities = new HashMap<>();

    private final Map<Class<?>, GraphQLOutputType> typeMappings = new HashMap<>();

    private final Map<String, GraphQLType> customTypes = new HashMap<>();

    private UnaryOperator<String> containerName = entityName -> format("%sResults", entityName);

    private UnaryOperator<String> filterName = entityName -> format("%sFilter", entityName);

    /// private static final Map<Enum<?>, GraphQLOutputType> defaultTypeMappings = new HashMap<>();

    private static final Map<Class<?>, GraphQLOutputType> defaultTypeMappings = new HashMap<>();

    private static final SchemaPrinter defaultSchemaPrinter = new SchemaPrinter(SchemaPrinter.Options.defaultOptions()
            .includeScalarTypes(true)
            .includeExtendedScalarTypes(true)
            .includeIntrospectionTypes(false)
            .includeDirectives(false)
            .includeSchemaDefinition(true));

    @Override
    @SuppressWarnings("unchecked")
    public <X> EntitySchema addEntity(Class<X> entityClass, Consumer<EntityDefinition<X>> consumer) {
        requireNonNull(entityClass, "entityClass");
        final EntityDefinitionImpl<X> entityDefinition = (EntityDefinitionImpl<X>) entities.computeIfAbsent(entityClass, unused -> new EntityDefinitionImpl<>(this, entityClass));
        if (consumer != null) {
            consumer.accept(entityDefinition);
        }
        return this;
    }

    @Override
    public EntitySchema setContainerName(UnaryOperator<String> containerName) {
        requireNonNull(containerName, "containerName");
        this.containerName = containerName;
        return this;
    }

    @Override
    public EntitySchema setFilterName(UnaryOperator<String> filterName) {
        requireNonNull(filterName, "filterName");
        this.filterName = filterName;
        return this;
    }

    public GraphQLSchema generate() {
        final GraphQLSchema.Builder builder = GraphQLSchema.newSchema();

        final GraphQLObjectType.Builder objectBuilder = new GraphQLObjectType.Builder().name("query");

        objectBuilder.field(newFieldDefinition()
                .name("listAllUsers")
                .argument(GraphQLArgument.newArgument().name("filter").type(GraphQLTypeReference.typeRef("UserFilter")).build())
                .argument(GraphQLArgument.newArgument().name("contextId").type(Scalars.GraphQLInt).build())
                .type(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef("UserResults"))));

        builder.query(objectBuilder);

        final SchemaBuilderContext ctx = new SchemaBuilderContext(this);

        for (EntityDefinitionImpl<?> metadata: entities.values()) {
            metadata.build(ctx);
        }

        ctx.generate(builder);

        builder.codeRegistry(GraphQLCodeRegistry.newCodeRegistry()
                .dataFetcher(FieldCoordinates.coordinates("query", "listAllUsers"), new RootDataFetcher(ctx, entities.values().iterator().next()))
//                .dataFetcher(FieldCoordinates.coordinates("User", "username"),  new Data2())
                .build());

        // builder.codeRegistry();

        // ;
        return builder.build();
    }

    String resolveContainerName(String entityName) {
        requireNonNull(entityName, "entityName");
        return containerName.apply(entityName);
    }

    String resolveFilterName(String entityName) {
        requireNonNull(entityName, "entityName");
        return filterName.apply(entityName);
    }

    EntityDefinitionImpl<?> getEntityFor(EntityResultType<?> type) {
        requireNonNull(type, "type");
        return entities.get(type.getJavaType());
    }

    GraphQLOutputType getUnwrappedTypeFor(SchemaBuilderContext ctx, Class<?> rawType) {
        requireNonNull(rawType, "rawType");
        if (typeMappings.containsKey(rawType)) {
            return typeMappings.get(rawType);
        }
        if (defaultTypeMappings.containsKey(rawType)) {
            return defaultTypeMappings.get(rawType);
        }
        return ctx.getUnwrappedTypeFor(rawType);
    }

    <E extends Enum<?>> GraphQLEnumType buildEnumTypeForClass(Class<E> enumClass) {
        requireNonNull(enumClass, "enumClass");
        final GraphQLEnumType.Builder typeBuilder = GraphQLEnumType
                .newEnum()
                .name(enumClass.getSimpleName());
        for (Enum<?> enumValue: enumClass.getEnumConstants()) {
            typeBuilder.value(enumValue.name(), enumValue);
        }
        return typeBuilder.build();
    }

    @Override
    public EntitySchema apply(Consumer<EntitySchema> consumer) {
        if (consumer != null) {
            consumer.accept(this);
        }
        return this;
    }

    @Override
    public String toString() {
        return defaultSchemaPrinter.print(generate());
    }

    private class RootDataFetcher implements DataFetcher {

        private SchemaBuilderContext ctx;

        private EntityDefinitionImpl<?> entityDefinition;

        private RootDataFetcher(SchemaBuilderContext ctx, EntityDefinitionImpl<?> entityDefinition) {
            this.ctx = ctx;
            this.entityDefinition = entityDefinition;
            System.out.println(entityDefinition);
        }

        @Override
        public String get(DataFetchingEnvironment dataFetchingEnvironment) throws Exception {
            System.out.println("********************************");
//            System.out.println(dataFetchingEnvironment);
//            System.out.println(dataFetchingEnvironment.getField());
//            System.out.println(dataFetchingEnvironment.getParentType());
//            System.out.println(dataFetchingEnvironment.getArguments());
//            System.out.println(dataFetchingEnvironment.getArguments().values());
//            System.out.println((Object) dataFetchingEnvironment.getSource());
//            System.out.println((Object) dataFetchingEnvironment.getContext());
//            System.out.println((Object) dataFetchingEnvironment.getRoot());

            final Map<String, Object> filter = (Map<String, Object>) dataFetchingEnvironment.getArguments().get("filter");

            System.out.println(ctx.applyEntityFilter(entityDefinition, filter));


            Thread.dumpStack();
            System.exit(0);
            return null; // ((User) dataFetchingEnvironment.getSource()).getDisplayName();
        }
    }

    static {

        // Primitives
        defaultTypeMappings.put(boolean.class, GraphQLNonNull.nonNull(Scalars.GraphQLBoolean));
        defaultTypeMappings.put(byte.class, GraphQLNonNull.nonNull(Scalars.GraphQLByte));
        defaultTypeMappings.put(short.class, GraphQLNonNull.nonNull(Scalars.GraphQLShort));
        defaultTypeMappings.put(int.class, GraphQLNonNull.nonNull(Scalars.GraphQLInt));
        defaultTypeMappings.put(long.class, GraphQLNonNull.nonNull(Scalars.GraphQLLong));
        defaultTypeMappings.put(char.class, GraphQLNonNull.nonNull(Scalars.GraphQLChar));
        defaultTypeMappings.put(float.class, GraphQLNonNull.nonNull(Scalars.GraphQLFloat));
        defaultTypeMappings.put(double.class, GraphQLNonNull.nonNull(Scalars.GraphQLFloat)); // No "double" type

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
                .type(GraphQLNonNull.nonNull(Scalars.GraphQLString)));
        builder.field(newFieldDefinition()
                .name("epoch_seconds")
                .type(GraphQLNonNull.nonNull(Scalars.GraphQLLong)));

        defaultTypeMappings.put(Instant.class, builder.build());

        // defaultTypeMappings.put(EitType.class, GraphQLEnumType.newEnum().name("EitType").value("value1", "456").value("value2", "789").build());
    }

    private static void addDefaultType(GraphQLScalarType graphType, Class<?>... javaTypes) {
        for (Class<?> javaType: javaTypes) {
            defaultTypeMappings.put(javaType, graphType);
        }
    }

}
