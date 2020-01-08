package org.grapple.schema;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.idl.SchemaPrinter;
import org.grapple.query.EntityResultType;

final class EntitySchemaImpl implements EntitySchema {

    private final Map<Class<?>, EntityDefinitionImpl<?>> entities = new HashMap<>();

    private final Map<Class<?>, GraphQLOutputType> typeMappings = new HashMap<>();

    private final Map<String, GraphQLType> customTypes = new HashMap<>();

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

//    @Override
//    public EntitySchema addCustomType(String typeAlias, GraphQLType type) {
//
//    }

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

    EntityDefinitionImpl<?> getEntity(EntityResultType<?> type) {
        requireNonNull(type, "type");
        return entities.get(type.getJavaType());
    }

    GraphQLOutputType getRawTypeFor(Class<?> rawType) {
        requireNonNull(rawType, "rawType");
        if (entities.containsKey(rawType)) {
            return GraphQLTypeReference.typeRef(entities.get(rawType).getName());
        }
        if (typeMappings.containsKey(rawType)) {
            return typeMappings.get(rawType);
        }
        if (defaultTypeMappings.containsKey(rawType)) {
            return defaultTypeMappings.get(rawType);
        }
        return null;
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

        final SchemaBuilderContext ctx = new SchemaBuilderContext();

        for (EntityDefinitionImpl<?> metadata: entities.values()) {
            metadata.build(ctx);
        }

        ctx.generate(builder);

        builder.codeRegistry(GraphQLCodeRegistry.newCodeRegistry()
                .dataFetcher(FieldCoordinates.coordinates("query", "listAllUsers"), new RootDataFetcher())
//                .dataFetcher(FieldCoordinates.coordinates("User", "username"),  new Data2())
                .build());

        // builder.codeRegistry();

        // ;
        return builder.build();
    }

    private static class RootDataFetcher implements DataFetcher {

        @Override
        public String get(DataFetchingEnvironment dataFetchingEnvironment) throws Exception {
            System.out.println("********************************");
            System.out.println(dataFetchingEnvironment);
            System.out.println(dataFetchingEnvironment.getField());
            System.out.println(dataFetchingEnvironment.getParentType());
            System.out.println(dataFetchingEnvironment.getArguments());
            System.out.println(dataFetchingEnvironment.getCacheControl());
            System.out.println((Object) dataFetchingEnvironment.getSource());
            System.out.println((Object) dataFetchingEnvironment.getContext());
            System.out.println((Object) dataFetchingEnvironment.getRoot());

            Object xxx = dataFetchingEnvironment.getArguments().get("filter");
            Object xxx2 = ((Map) xxx).get("id_in");

            Thread.dumpStack();
            System.exit(0);
            return null; // ((User) dataFetchingEnvironment.getSource()).getDisplayName();
        }
    }

    static {
        addDefaultType(Scalars.GraphQLInt, int.class, Integer.class);
        addDefaultType(Scalars.GraphQLFloat, float.class, Float.class);
        addDefaultType(Scalars.GraphQLString, String.class);
        addDefaultType(Scalars.GraphQLBoolean, boolean.class, Boolean.class);
        addDefaultType(Scalars.GraphQLLong, long.class, Long.class);
        addDefaultType(Scalars.GraphQLShort, short.class, Short.class);
        addDefaultType(Scalars.GraphQLByte, byte.class, Byte.class);
        addDefaultType(Scalars.GraphQLBigInteger, BigInteger.class);
        addDefaultType(Scalars.GraphQLBigDecimal, BigDecimal.class);
        addDefaultType(Scalars.GraphQLChar, char.class, Character.class);

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
