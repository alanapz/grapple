package org.grapple.schema.impl;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import static graphql.schema.GraphQLObjectType.newObject;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.schema.impl.RuntimeWiring.FieldFilterWiring.fieldFilterWiring;
import static org.grapple.schema.impl.SchemaBuilderContextUtils.enumTypeCache;
import static org.grapple.utils.Utils.readOnlyCopy;
import static org.jooq.lambda.Seq.seq;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import graphql.language.Field;
import graphql.language.SelectionSet;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import org.grapple.core.ElementVisibility;
import org.grapple.query.EntityFilter;
import org.grapple.query.EntityResultType;
import org.grapple.query.FetchSet;
import org.grapple.query.Filters;
import org.grapple.query.QueryField;
import org.grapple.query.QueryResultList;
import org.grapple.query.QueryResultRow;
import org.grapple.query.RootFetchSet;
import org.grapple.reflect.ClassLiteral;
import org.grapple.reflect.ReflectUtils;
import org.grapple.reflect.TypeConverter;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityDefinition;
import org.grapple.schema.EntityQueryType;
import org.grapple.schema.EnumTypeBuilder;
import org.grapple.schema.TypeNotMappedException;
import org.grapple.schema.impl.RuntimeWiring.EntityFilterWiring;
import org.grapple.schema.impl.RuntimeWiring.EntityOrderByWiring;
import org.grapple.schema.impl.RuntimeWiring.EntityQueryParameterWiring;
import org.grapple.schema.impl.RuntimeWiring.EntityQueryWiring;
import org.grapple.schema.impl.RuntimeWiring.EntitySelectionWiring;
import org.grapple.schema.impl.RuntimeWiring.FieldFilterWiring;
import org.grapple.schema.impl.SchemaBuilderContextUtils.EnumTypeCache;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple.utils.UnexpectedException;
import org.jooq.lambda.tuple.Tuple2;

final class SchemaBuilderContext implements org.grapple.schema.SchemaBuilderContext {

    private final EntitySchemaImpl schema;

    private final Set<String> rolesHeld;

    private final Map<Type, GraphQLObjectType> unmanagedTypes = new NoDuplicatesMap<>(new LinkedHashMap<>());

    private final Map<FieldCoordinates, DataFetcher<?>> unmanagedDataFetchers = new NoDuplicatesMap<>();

    private final TypeConverter typeConverter;

    private final EntityQueryExecutionListeners entityQueryExecutionListeners;

    private final Map<EntityDefinition<?>, GraphQLObjectType.Builder> entityTypes = new HashMap<>();

    private final Map<EntityDefinition<?>, GraphQLObjectType.Builder> containerTypes = new HashMap<>();

    private final Map<EntityDefinitionImpl<?>, EntityOrderByDefinitionImpl<?>> entityOrderByTypes = new HashMap<>();

    private final Map<Tuple2<Class<?>, String>, EntitySelectionWiring<?>> entitySelectionWirings = new NoDuplicatesMap<>();

    private final Map<Class<?>, GeneratedEntityFilter<?>> entityFilters = new NoDuplicatesMap<>();

    // [Entity Class, Field Name] -> EntityFilterWiring
    private final Map<Tuple2<Class<?>, String>, EntityFilterWiring<?>> entityFilterWirings = new NoDuplicatesMap<>();

    // [Entity Class, Field Name] -> EntityOrderByWiring
    private final Map<Tuple2<Class<?>, String>, EntityOrderByWiring<?>> entityOrderByWirings = new NoDuplicatesMap<>();

    // Query Name -> EntityQueryWiring
    private final Map<String, EntityQueryWiring<?>> entityQueryWirings = new NoDuplicatesMap<>();

    // [Query Name, Parameter Name] -> EntityQueryParameterWiring
    private final Map<Tuple2<String, String>, EntityQueryParameterWiring<?>> entityQueryParameterWirings = new NoDuplicatesMap<>();

    private final Map<Type, GraphQLInputType> inputTypeCache = new NoDuplicatesMap<>();

    private final EnumTypeCache enumTypeCache;

    private final Map<TypeLiteral<?>, GeneratedFieldFilter<?>> fieldFilters = new NoDuplicatesMap<>();

    private final Map<Tuple2<TypeLiteral<?>, String>, FieldFilterWiring<?>> fieldFilterWirings = new NoDuplicatesMap<>();

    private final Map<String, GraphQLFieldDefinition> rootQueryFields = new NoDuplicatesMap<>();

    private final String rootQueryTypeName;

    private final SchemaBuilderElementVisibility schemaBuilderElementVisibility;

    SchemaBuilderContext(EntitySchemaImpl schema,
                         Set<String> rolesHeld,
                         EnumTypeBuilder enumTypeBuilder,
                         TypeConverter typeConverter,
                         EntityQueryExecutionListeners entityQueryExecutionListeners,
                         String rootQueryTypeName) {
        this.schema = requireNonNull(schema, "schema");
        this.rolesHeld = readOnlyCopy(rolesHeld);
        this.enumTypeCache = enumTypeCache(this, enumTypeBuilder);
        this.typeConverter = requireNonNull(typeConverter, "typeConverter");
        this.entityQueryExecutionListeners = requireNonNull(entityQueryExecutionListeners, "entityQueryExecutionListeners").copy();
        this.rootQueryTypeName = requireNonNull(rootQueryTypeName, "rootQueryTypeName");
        this.schemaBuilderElementVisibility = new SchemaBuilderElementVisibility(rootQueryTypeName);
    }

    EntitySchemaImpl getSchema() {
        return schema;
    }

    String getRootQueryTypeName() {
        return rootQueryTypeName;
    }

    boolean isSchemaElementVisible(ElementVisibility visibility) {
        return (visibility == null || rolesHeld == null || visibility.isVisible(rolesHeld));
    }

    void addEntityType(EntityDefinition<?> entity, GraphQLObjectType.Builder entityType) {
        requireNonNull(entity, "entity");
        requireNonNull(entityType, "entityType");
        entityTypes.put(entity, entityType);
    }

    void addContainerType(EntityDefinition<?> entity, GraphQLObjectType.Builder containerType) {
        requireNonNull(entity, "entity");
        requireNonNull(containerType, "containerType");
        containerTypes.put(entity, containerType);
    }

    void addEntityFilter(Class<?> entityClass, GeneratedEntityFilter<?> entityFilter) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(entityFilter, "entityFilter");
        entityFilters.put(entityClass, entityFilter);
    }

    void addUnmanagedQuery(Type type, GraphQLObjectType graphQLType) {
        requireNonNull(type, "type");
        requireNonNull(graphQLType, "graphQLType");
        unmanagedTypes.put(type, graphQLType);
    }

    void addUnmanagedType(Type type, GraphQLObjectType graphQLType) {
        requireNonNull(type, "type");
        requireNonNull(graphQLType, "graphQLType");
        unmanagedTypes.put(type, graphQLType);
    }

    void addUnmanagedDataFetcher(String typeName, String fieldName, DataFetcher<?> dataFetcher) {
        requireNonNull(typeName, "typeName");
        requireNonNull(fieldName, "fieldName");
        requireNonNull(dataFetcher, "dataFetcher");
        unmanagedDataFetchers.put(coordinates(typeName, fieldName), dataFetcher);
    }

    @SuppressWarnings("unchecked")
    <X> EntityOrderByDefinitionImpl<X> getEntityOrderBy(EntityDefinitionImpl<X> entityDefinition) {
        requireNonNull(entityDefinition, "entityDefinition");
        return (EntityOrderByDefinitionImpl<X>) entityOrderByTypes.get(entityDefinition);
    }

    <X> void putEntityOrderBy(EntityDefinitionImpl<X> entityDefinition, EntityOrderByDefinitionImpl<X> orderByDefinition) {
        requireNonNull(entityDefinition, "entityDefinition");
        requireNonNull(orderByDefinition, "orderByDefinition");
        entityOrderByTypes.put(entityDefinition, orderByDefinition);
    }

    <X> void addEntitySelectionWiring(EntitySelectionWiring<X> entitySelectionWiring) {
        requireNonNull(entitySelectionWiring, "entitySelectionWiring");
        entitySelectionWirings.put(new Tuple2<>(entitySelectionWiring.getEntityClass(), entitySelectionWiring.getFieldName()), entitySelectionWiring);
    }

    @SuppressWarnings("unchecked")
    <X> void applyEntitySelection(DataFetchingEnvironment environment, Class<X> entityClass, FetchSet<X> fetchSet, SelectionSet selectionSet) {
        requireNonNull(environment, "environment");
        requireNonNull(entityClass, "entityClass");
        requireNonNull(fetchSet, "fetchSet");
        requireNonNull(selectionSet, "selectionSet");
        for (Field field: seq(selectionSet.getSelections()).filter(SchemaUtils::isQueryableField).cast(Field.class)) {
            final Tuple2<Class<?>, String> selectionKey = new Tuple2<>(entityClass, field.getName());
            final EntitySelectionWiring<X> selectionWiring = (EntitySelectionWiring<X>) entitySelectionWirings.get(selectionKey);
            if (selectionWiring == null) {
                throw new UnexpectedException(format("Entity selection wiring not found: %s", selectionKey));
            }
            selectionWiring.addSelection(this, environment, fetchSet, field);
        }
    }

    @SuppressWarnings("unchecked")
    <X> Map<String, Object> parseQueryResponse(DataFetchingEnvironment environment, Class<X> entityClass, SelectionSet selectionSet, QueryResultRow<X> resultRow) {
        requireNonNull(environment, "environment");
        requireNonNull(entityClass, "entityClass");
        requireNonNull(selectionSet, "selectionSet");
        requireNonNull(resultRow, "resultRow");
        final Map<String, Object> response = new HashMap<>();
        for (Field field: seq(selectionSet.getSelections()).filter(SchemaUtils::isQueryableField).cast(Field.class)) {
            final Tuple2<Class<?>, String> selectionKey = new Tuple2<>(entityClass, field.getName());
            final EntitySelectionWiring<X> selectionWiring = (EntitySelectionWiring<X>) entitySelectionWirings.get(selectionKey);
            if (selectionWiring == null) {
                throw new UnexpectedException(format("Entity selection wiring not found: %s", selectionKey));
            }
            response.put(field.getName(), selectionWiring.resolveResponse(this, environment, resultRow, field));
        }
        return response;
    }

    <X> void addEntityFilterWiring(EntityFilterWiring<X> entityFilterWiring) {
        requireNonNull(entityFilterWiring, "entityFilterWiring");
        entityFilterWirings.put(new Tuple2<>(entityFilterWiring.getEntityClass(), entityFilterWiring.getFieldName()), entityFilterWiring);
    }

    <X> void addEntityOrderByWiring(EntityOrderByWiring<X> entityOrderByWiring) {
        requireNonNull(entityOrderByWiring, "entityOrderByWiring");
        entityOrderByWirings.put(new Tuple2<>(entityOrderByWiring.getEntityClass(), entityOrderByWiring.getFieldName()), entityOrderByWiring);
    }

    void addRootQueryField(GraphQLFieldDefinition fieldDefinition) {
        requireNonNull(fieldDefinition, "fieldDefinition");
        rootQueryFields.put(fieldDefinition.getName(), fieldDefinition);
    }

    <X> void addEntityQueryWiring(EntityQueryWiring<X> entityQueryWiring) {
        requireNonNull(entityQueryWiring, "entityQueryWiring");
        entityQueryWirings.put(entityQueryWiring.getQueryName(), entityQueryWiring);
    }

    <T> void addEntityQueryParameterWiring(EntityQueryParameterWiring<T> entityQueryParameterWiring) {
        requireNonNull(entityQueryParameterWiring, "entityQueryParameterWiring");
        entityQueryParameterWirings.put(new Tuple2<>(entityQueryParameterWiring.getQueryName(), entityQueryParameterWiring.getParameterName()), entityQueryParameterWiring);
    }

    @SuppressWarnings("unchecked")
    <X> QueryResultList<X> executeEntityQuery(DataFetchingEnvironment environment, Class<X> entityClass, String queryName, RootFetchSet<X> fetchSet, Map<String, Object> args) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(queryName, "queryName");
        requireNonNull(fetchSet, "fetchSet");
        requireNonNull(args, "args");
        final EntityQueryWiring<X> queryWiring = (EntityQueryWiring<X>) entityQueryWirings.get(queryName);
        if (queryWiring == null) {
            throw new UnexpectedException(format("Entity query wiring not found: %s", queryName));
        }
        final Map<String, Object> queryParameters = new HashMap<>();
        for (Map.Entry<String, Object> arg: args.entrySet()) {
            final EntityQueryParameterWiring<?> queryParameterWiring = entityQueryParameterWirings.get(new Tuple2<>(queryName, arg.getKey()));
            if (queryParameterWiring != null) {
                queryParameters.put(arg.getKey(), typeConverter.convertObjectToType(queryParameterWiring.getParameterType(), arg.getValue()));
            }
        }
        return queryWiring.executeQuery(this, environment, fetchSet, queryParameters);
    }

    @SuppressWarnings("unchecked")
    <X> EntityFilter<X> generateEntityFilter(DataFetchingEnvironment environment, Class<X> entityClass, FetchSet<X> fetchSet, Map<String, Object> args) {
        requireNonNull(environment, "environment");
        requireNonNull(entityClass, "entityClass");
        requireNonNull(fetchSet, "fetchSet");
        requireNonNull(args, "args");

        final List<EntityFilter<X>> filters = new ArrayList<>();

        args.forEach((argName, argValue) -> {
            if (argName == null || argValue == null) {
                return;
            }
            final Tuple2<Class<?>, String> filterKey = new Tuple2<>(entityClass, argName);
            final EntityFilterWiring<X> entityFilterWiring = (EntityFilterWiring<X>) entityFilterWirings.get(filterKey);
            if (entityFilterWiring == null) {
                throw new UnexpectedException(format("Unmapped entity filter: %s", filterKey));
            }
            final EntityFilter<X> entityFilter = entityFilterWiring.resolveFilter(this, environment, fetchSet, argValue);
            if (entityFilter != null) {
                filters.add(entityFilter);
            }
        });

        return Filters.and(filters);
    }

    @SuppressWarnings("unchecked")
    <T> GeneratedFieldFilter<T> buildFieldFilter(EntityResultType<T> resultType) {
        requireNonNull(resultType, "resultType");
        final TypeLiteral<T> fieldType = resultType.getType().wrapPrimitiveTypeIfNecessary();
        // We don't support generic filters
        if (!(fieldType instanceof ClassLiteral<?>)) {
            return null;
        }
        // Nor arrays
        final Class<T> clazz = ((ClassLiteral<T>) resultType.getType()).getType();
        if (clazz.isArray()) {
            return null;
        }
        // If we already exist, return created filter
        if (fieldFilters.containsKey(fieldType)) {
            return (GeneratedFieldFilter<T>) fieldFilters.get(fieldType);
        }
        // Otherwise, attempt to use user-defined filter if specified, default otherwise
        final FieldFilterDefinitionImpl<T> filterDefinition = schema.generateFieldFilter(fieldType, getInputTypeFor(clazz));
        if (filterDefinition == null) {
            return null;
        }
        final GeneratedFieldFilter<T> generatedFilter = new GeneratedFieldFilter<>(filterDefinition);
        fieldFilters.put(fieldType, generatedFilter);

        for (SimpleFieldFilterItem<T> x: generatedFilter.items.values()) {
            fieldFilterWirings.put(new Tuple2<>(generatedFilter.getFilterType(), x.name), fieldFilterWiring(generatedFilter.getFilterType(), x.name, x.resolver));

        }

        return generatedFilter;
    }

    @SuppressWarnings("unchecked")
    <X> void applyEntityOrderBy(DataFetchingEnvironment environment, Class<X> entityClass, FetchSet<X> fetchSet, Map<String, Object> args) {
        requireNonNull(environment, "environment");
        requireNonNull(entityClass, "entityClass");
        requireNonNull(fetchSet, "fetchSet");
        requireNonNull(args, "args");

        args.forEach((argName, argValue) -> {
            if (argName == null || argValue == null) {
                return;
            }
            final Tuple2<Class<?>, String> orderByKey = new Tuple2<>(entityClass, argName);
            final EntityOrderByWiring<X> entityOrderByWiring = (EntityOrderByWiring<X>) entityOrderByWirings.get(orderByKey);
            if (entityOrderByWiring == null) {
                throw new UnexpectedException(format("Unmapped entity order by item: %s.%s", entityClass.getName(), argName));
            }
            entityOrderByWiring.applyOrderBy(this, environment, fetchSet, argValue);
        });
    }

    @SuppressWarnings("unchecked")
    <X, T> EntityFilter<X> generateFieldFilter(DataFetchingEnvironment environment, TypeLiteral<T> filterType, QueryField<X, T> queryField, Map<String, Object> args) {
        requireNonNull(environment, "environment");
        requireNonNull(filterType, "filterType");
        requireNonNull(queryField, "queryField");
        requireNonNull(args, "args");

        // eg: userName: {id: 1, age: 2, country: 'UK'} -> (id = 1 AND age == 2 AND country == 'UK)

        final List<EntityFilter<X>> filters = new ArrayList<>();

        args.forEach((argName, argValue) -> {
            if (argName == null || argValue == null) {
                return;
            }
            final Tuple2<TypeLiteral<?>, String> filterKey = new Tuple2<>(filterType, argName);
            final FieldFilterWiring<T> fieldFilterWiring = (FieldFilterWiring<T>) fieldFilterWirings.get(filterKey);
            if (fieldFilterWiring == null) {
                throw new UnexpectedException(format("Unmapped field filter item: %s.%s", filterType.getType().getTypeName(), argName));
            }
            filters.add(fieldFilterWiring.resolveFilter(environment, queryField, argValue));
        });

        return Filters.and(filters);
    }

    GraphQLInputType getInputTypeFor(Type type) {
        requireNonNull(type, "type");
        final GraphQLInputType result = inputTypeCache.computeIfAbsent(type, this::computeInputTypeFor);
        if (result == null) {
            throw new TypeNotMappedException(format("Unable to resolve input type for: %s", type.getTypeName()));
        }
        return result;
    }

    private GraphQLInputType computeInputTypeFor(Type type) {
        requireNonNull(type, "type");

        // Check to see whether we are an existing enum type
        final GraphQLEnumType enumType = enumTypeCache.buildIfEnumType(type);
        if (enumType != null) {
            return enumType;
        }

        // We may be an array of enums / scalars
        if (type instanceof Class<?> && ((Class<?>) type).isArray()) {
            final GraphQLInputType componentType = computeInputTypeFor(((Class<?>) type).getComponentType());
            if (componentType != null) {
                return GraphQLList.list(componentType);
            }
        }

        // We may also be a List<T> or other collection
        if (type instanceof ParameterizedType) {
            final Class<?> klazz = ReflectUtils.getRawTypeFor(type);
            if (klazz != null && Collection.class.isAssignableFrom(klazz)) {
                final GraphQLInputType componentType = computeInputTypeFor(ReflectUtils.getGenericTypeArgument(type, 0));
                if (componentType != null) {
                    return GraphQLList.list(componentType);
                }
            }
        }

        // Or even a generic array of input types
        if (type instanceof GenericArrayType) {
            final GraphQLInputType componentType = computeInputTypeFor(((GenericArrayType) type).getGenericComponentType());
            if (componentType != null) {
                return GraphQLList.list(componentType);
            }
        }

        // Check to see whether we are an existing scalar type
        final GraphQLInputType defaultType = DefaultTypeMappings.getDefaultInputTypeFor(type);
        if (defaultType != null) {
            return defaultType;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    GraphQLOutputType getUnwrappedTypeFor(Type type) {
        requireNonNull(type, "type");

        final GraphQLObjectType unmanagedType = unmanagedTypes.get(type);
        if (unmanagedType != null) {
            return unmanagedType;
        }
        final GraphQLEnumType enumType = enumTypeCache.buildIfEnumType(type);
        if (enumType != null) {
            return enumType;
        }

        throw new TypeNotMappedException(format("Type: %s not mapped", type.getTypeName()));
    }

    public void generate(GraphQLSchema.Builder schemaBuilder) {
        for (Map.Entry<?, GraphQLObjectType.Builder> entry: entityTypes.entrySet()) {
            schemaBuilder.additionalType(entry.getValue().build());
        }
        for (Map.Entry<?, GraphQLObjectType.Builder> entry: containerTypes.entrySet()) {
            schemaBuilder.additionalType(entry.getValue().build());
        }
        for (EntityOrderByDefinitionImpl<?> orderByDefinition: entityOrderByTypes.values()) {
            schemaBuilder.additionalType(orderByDefinition.build(this));
        }
        for (GraphQLEnumType enumType: enumTypeCache.getAllEnumTypes()) {
            schemaBuilder.additionalType(enumType);
        }
        for (GeneratedEntityFilter<?> entityFilter: entityFilters.values()) {
            schemaBuilder.additionalType(entityFilter.build(this));
        }
        for (GeneratedFieldFilter<?> fieldFilter: fieldFilters.values()) {
            schemaBuilder.additionalType(fieldFilter.build(this));
        }
        for (GraphQLObjectType unmanagedType: unmanagedTypes.values()) {
            schemaBuilder.additionalType(unmanagedType);
        }

        schemaBuilder.query(buildRootQueryObject());
        schemaBuilder.codeRegistry(buildCodeRegistry());
    }

    // Query field - the root object of the GraphQL system
    private GraphQLObjectType buildRootQueryObject() {
        final GraphQLObjectType.Builder queryObjectBuilder = newObject();
        queryObjectBuilder.name(rootQueryTypeName);
        for (GraphQLFieldDefinition rootQueryField: rootQueryFields.values()) {
            queryObjectBuilder.field(rootQueryField);
        }
        return queryObjectBuilder.build();
    }


    // Code registry is where we define our resolvers
    private GraphQLCodeRegistry buildCodeRegistry() {
        final GraphQLCodeRegistry.Builder codeRegistry = newCodeRegistry();

        for (EntityQueryWiring<?> entityQueryWiring: entityQueryWirings.values()) {
            final FieldCoordinates fieldCoordinates = coordinates(rootQueryTypeName, entityQueryWiring.getQueryName());
            if (entityQueryWiring.getQueryType() == EntityQueryType.LIST) {
                codeRegistry.dataFetcher(fieldCoordinates, new EntityListQueryDataFetcher<>(this, entityQueryWiring.getEntityClass(), entityQueryWiring.getQueryName(), null));
            }
            if (entityQueryWiring.getQueryType() == EntityQueryType.SCALAR_NON_NULL || entityQueryWiring.getQueryType() == EntityQueryType.SCALAR_NULL_ALLOWED) {
                codeRegistry.dataFetcher(fieldCoordinates, new EntityScalarQueryDataFetcher<>(this, entityQueryWiring.getEntityClass(), entityQueryWiring.getQueryName(), null));
            }
        }

        for (Map.Entry<FieldCoordinates, DataFetcher<?>> unmanagedDataFetcher: unmanagedDataFetchers.entrySet()) {
            codeRegistry.dataFetcher(unmanagedDataFetcher.getKey(), unmanagedDataFetcher.getValue());
        }

        return codeRegistry.build();
    }

    public <T> T convertInput(TypeLiteral<T> type, Object object) {
        return typeConverter.convertObjectToType(type, object);
    }

    EntityQueryExecutionListeners getEntityQueryExecutionListeners() {
        return entityQueryExecutionListeners;
    }

    SchemaBuilderElementVisibility getSchemaBuilderElementVisibility() {
        return schemaBuilderElementVisibility;
    }

    @Override
    public GraphQLType buildTypeFor(Type type) {
        return schema.getUnwrappedTypeFor(this, type);
    }
}
