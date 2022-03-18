package org.grapple.schema.impl;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.reflect.ReflectUtils.getGenericTypeArgument;
import static org.grapple.utils.Utils.isNotEmpty;
import static org.grapple.utils.Utils.toSet;
import static org.jooq.lambda.Seq.of;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.grapple.invoker.GrappleParameter;
import org.grapple.invoker.GrappleQuery;
import org.grapple.metadata.DeprecationReason;
import org.grapple.metadata.FieldNotExported;
import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityMetadataKeys;
import org.grapple.query.QueryResultList;
import org.grapple.query.QueryResultRow;
import org.grapple.query.RootFetchSet;
import org.grapple.reflect.EntityFilterMethodMetadata;
import org.grapple.reflect.EntityQueryMetadata.EntityQueryMethodMetadata;
import org.grapple.reflect.EntityQueryMetadata.EntityQueryMethodType;
import org.grapple.reflect.EntityQueryMetadata.EntityQueryParameterMetadata;
import org.grapple.reflect.InvokerMetadataFactory;
import org.grapple.reflect.ReflectUtils;
import org.grapple.schema.DefinitionImportException;
import org.grapple.schema.EntityFilterItemBuilder;
import org.grapple.schema.EntityQueryResolver;
import org.grapple.schema.EntityQueryType;
import org.grapple.schema.EntitySchemaScanner;
import org.grapple.schema.EntitySchemaScannerCallback;
import org.grapple.utils.UnreachableException;

final class EntitySchemaScannerImpl implements EntitySchemaScanner {

    private final EntitySchemaImpl schema;

    private final EntitySchemaScannerCallback scannerCallback;

    EntitySchemaScannerImpl(EntitySchemaImpl schema, EntitySchemaScannerCallback scannerCallback) {
        this.schema = requireNonNull(schema, "schema");
        this.scannerCallback = requireNonNull(scannerCallback, "scannerCallback");
    }

    @Override
    public void importDefinitions(Class<? extends Annotation> annotation, String... packageNames) {
        requireNonNull(annotation, "annotation");
        requireNonNull(packageNames, "packageNames");
        for (Class<?> definitionsClass: ReflectUtils.getAllTypesAnnotatedWith(toSet(packageNames), annotation,false)) {
            importDefinitions(definitionsClass);
        }
    }

    @Override
    public void importDefinitions(Class<?> definitionsClass) {
        requireNonNull(definitionsClass, "definitionsClass");
        for (Field field : definitionsClass.getDeclaredFields()) {
            try {
                if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                    importDefinition(field, field.getGenericType(), field.get(null));
                }
            }
            catch (IllegalAccessException e) {
                throw new DefinitionImportException(format("Couldn't import field definitions from: %s", field), e);
            }
        }
        for (Method method : definitionsClass.getDeclaredMethods()) {
            final EntityFilterMethodMetadata<?, ?> entityFilterMetadata = InvokerMetadataFactory.parseFilterMethod(method);
            if (entityFilterMetadata != null) {
                importFilter(entityFilterMetadata);
            }
        }
    }

    private void importDefinition(Field source, Type type, Object value) {
        if (source.isAnnotationPresent(FieldNotExported.class)) {
            return;
        }
        if ((value instanceof EntityField<?, ?>) || (value instanceof EntityJoin<?, ?>)) {
            processEntityFieldOrJoin(source, (Class<?>) getGenericTypeArgument(type, 0), value);
        }
        if (value instanceof Collection<?>) {
            final Type componentType = getGenericTypeArgument(type, 0);
            ((Collection<?>) value).forEach(childItem -> importDefinition(source, componentType, childItem));
        }
        if (value instanceof Map<?, ?>) {
            final Type componentType = getGenericTypeArgument(type, 1); // (0 = Key, 1 = Value)
            ((Map<?, ?>) value).values().forEach(childItem -> importDefinition(source, componentType, childItem));
        }
    }

    private <X, Y, T> void processEntityFieldOrJoin(Field source, Class<X> entityClass, Object value) {
        final EntityDefinitionImpl<X> entityDefinition = findOrLoadEntity(entityClass);
        if (entityDefinition == null) {
            return;
        }
        if (value instanceof EntityField<?, ?>) {
            final @SuppressWarnings("unchecked") EntityField<X, T> entityField = (EntityField<X, T>) value;
            if (Boolean.TRUE.equals(entityField.getMetadata(EntityMetadataKeys.FieldNotExported))) {
                return;
            }
            if (!scannerCallback.acceptField(entityField)) {
                return;
            }
            entityDefinition.addField(entityField).apply(fieldDefinition -> scannerCallback.configureField(source, fieldDefinition));
        }
        if (value instanceof EntityJoin<?, ?>) {
            final @SuppressWarnings("unchecked") EntityJoin<X, Y> entityJoin = (EntityJoin<X, Y>) value;
            if (Boolean.TRUE.equals(entityJoin.getMetadata(EntityMetadataKeys.FieldNotExported))) {
                return;
            }
            if (!scannerCallback.acceptJoin(entityJoin)) {
                return;
            }
            entityDefinition.addJoin(entityJoin).apply(joinDefinition -> scannerCallback.configureJoin(source, joinDefinition));
        }
    }

    private <X, T> void importFilter(EntityFilterMethodMetadata<X, T> methodMetadata) {
        final EntityDefinitionImpl<X> entityDefinition = findOrLoadEntity(methodMetadata.getEntityClass());
        if (entityDefinition == null) {
            return;
        }
        final EntityFilterItemBuilder<X, T> builder = new EntityFilterItemBuilder<X, T>()
                .setName(methodMetadata.getFilterName())
                .setDescription(methodMetadata.getDescription())
                .setDeprecationReason(methodMetadata.getDeprecationReason())
                .setFilterResolver(methodMetadata::generate);
        if (!scannerCallback.acceptFilter(methodMetadata.getMethod(), entityDefinition, builder)) {
            return;
        }
        entityDefinition.addFilterItem(methodMetadata.getParameterType(), consumer -> {
            consumer.setName(builder.getName());
            consumer.setDescription(builder.getDescription());
            consumer.setDeprecationReason(builder.getDeprecationReason());
            consumer.setVisibility(builder.getVisibility());
            consumer.setFilterResolver(builder.getFilterResolver());
        });
    }

    @Override
    public void importOperations(Object instance) {
        requireNonNull(instance, "instance");
        importOperations(instance, instance.getClass());
    }

    @Override
    public void importOperations(Object instance, Class<?> instanceClass) {
        requireNonNull(instance, "instance");
        requireNonNull(instanceClass, "instanceClass");
        for (Method method : instanceClass.getDeclaredMethods()) {
            final EntityQueryMethodMetadata<?> entityQueryMetadata = processQueryMethod(method);
            if (entityQueryMetadata != null) {
                importQueryResult(instance, entityQueryMetadata);
            }
        }
    }

    private <X> void importQueryResult(Object instance, EntityQueryMethodMetadata<X> entityQueryMetadata) {
        requireNonNull(instance, "instance");
        requireNonNull(entityQueryMetadata, "entityQueryMetadata");
        final EntityDefinitionImpl<X> entityDefinition = findOrLoadEntity(entityQueryMetadata.entityClass);
        if (entityDefinition == null) {
            return;
        }
        if (!scannerCallback.acceptQuery(entityDefinition, entityQueryMetadata.method)) {
            return;
        }
        final EntityQueryResolver<X> entityQueryResolver = EntityQueryUtils.buildMethodEntityQueryResolver(entityQueryMetadata, instance);
        final GeneratedEntityQueryDefinitionImpl<X> entityQueryDefinition = entityDefinition.addGeneratedQuery(entityQueryMetadata, entityQueryResolver);
        scannerCallback.configureQuery(entityQueryMetadata.method, entityQueryDefinition);
    }

    private <X> EntityDefinitionImpl<X> findOrLoadEntity(Class<X> entityClass) {
        requireNonNull(entityClass, "entityClass");
        final EntityDefinitionImpl<X> existing = schema.getEntity(entityClass);
        if (existing != null) {
            return existing;
        }
        if (!scannerCallback.acceptEntity(entityClass)) {
            return null;
        }
        final EntityDefinitionImpl<X> entityDefinition = schema.addEntity(entityClass);
        scannerCallback.configureEntity(entityClass, entityDefinition);
        return entityDefinition;
    }

    private static EntityQueryMethodMetadata<?> processQueryMethod(Method method) {
        requireNonNull(method, "method");

        final GrappleQuery grappleQuery = ReflectUtils.searchMethodAnnotation(method, GrappleQuery.class).orElse(null);
        // We are only interested in methods annotated with @GrappleQuery
        if (grappleQuery == null) {
            return null;
        }

        // Skip ignored methods (only useful if one day we include all methods even those without @GrappleQuery)
        if (grappleQuery.ignore()) {
            return null;
        }

        // The entity is the concrete type parameter of the RootFetchSet<>
        // It is an error to not have a concrete type
        final Parameter fetchSetArgument = of(method.getParameters())
                .filter(p -> RootFetchSet.class.isAssignableFrom(p.getType()))
                .findSingle()
                .orElse(null);
        if (fetchSetArgument == null) {
            throw new DefinitionImportException("Couldn't retrieve RootFetchSet parameter", method);
        }
        if (!(fetchSetArgument.getParameterizedType() instanceof ParameterizedType)) {
            throw new DefinitionImportException("Unexpected RootFetchSet parameter type", method);
        }
        final ParameterizedType fetchSetArgumentType = (ParameterizedType) fetchSetArgument.getParameterizedType();
        if (fetchSetArgumentType.getActualTypeArguments().length != 1) {
            throw new DefinitionImportException("Unexpected RootFetchSet parameter type", method);
        }
        final Type entityClass = fetchSetArgumentType.getActualTypeArguments()[0];
        if (!(entityClass instanceof Class<?>)) {
            throw new DefinitionImportException(format("Unexpected RootFetchSet entity type: %s", entityClass), method);
        }

        final EntityQueryMethodType queryMethodType = parseQueryMethodType(method, entityClass);
        final EntityQueryType entityQueryType = parseEntityQueryType(queryMethodType);

        final EntityQueryMethodMetadata<?> methodMetadata = new EntityQueryMethodMetadata<>((Class<?>) entityClass,
                method,
                queryMethodType,
                (isNotEmpty(grappleQuery.value()) ? grappleQuery.value() : method.getName()),
                entityQueryType,
                (isNotEmpty(grappleQuery.description()) ? grappleQuery.description() : null));

        if (isNotEmpty(grappleQuery.deprecated())) {
            methodMetadata.deprecationReason = grappleQuery.deprecated();
        }
        else if (method.isAnnotationPresent(DeprecationReason.class)) {
            methodMetadata.deprecationReason = method.getAnnotation(DeprecationReason.class).value();
        }
        else if (method.isAnnotationPresent(Deprecated.class)) {
            methodMetadata.deprecationReason = "Deprecated";
        }

        for (int index = 0; index < method.getParameterCount(); index++) {
            final Parameter parameter = method.getParameters()[index];
            if (parameter == fetchSetArgument) {
                methodMetadata.fetchSetParameterIndex = index;
                continue;
            }
            final EntityQueryParameterMetadata parameterMetadata = processQueryParameter(parameter, index);
            if (parameterMetadata != null) {
                methodMetadata.parameters.put(parameterMetadata.name, parameterMetadata);
            }
        }

        return methodMetadata;
    }

    private static EntityQueryMethodType parseQueryMethodType(Method method, Type entityClass) {
        final Type returnType = method.getGenericReturnType();
        // Return type is QueryResultList<XXX> - LIST
        if (QueryResultList.class.equals(ReflectUtils.getRawTypeFor(returnType))) {
            // Make sure generic type matches entity type
            final Type queryResultListType = getGenericTypeArgument(returnType, 0);
            if (!entityClass.equals(ReflectUtils.getRawTypeFor(queryResultListType))) {
                throw new DefinitionImportException(format("Unexpected QueryResultList parameter type: %s", queryResultListType), method);
            }
            return EntityQueryMethodType.LIST;
        }
        // Return type is QueryResultRow<XXX> - SCALAR_NON_NULL
        if (QueryResultRow.class.equals(ReflectUtils.getRawTypeFor(returnType))) {
            // Make sure generic type matches entity type
            final Type queryResultRowType = getGenericTypeArgument(returnType, 0);
            if (!entityClass.equals(ReflectUtils.getRawTypeFor(queryResultRowType))) {
                throw new DefinitionImportException(format("Unexpected QueryResultRow parameter type: %s", queryResultRowType), method);
            }
            return EntityQueryMethodType.ROW;
        }
        // Return type is Optional<QueryResultRow<XXX>> - SCALAR NULL
        if (Optional.class.equals(ReflectUtils.getRawTypeFor(returnType))) {
            // Make sure generic type matches entity type
            final Type optionalType = getGenericTypeArgument(returnType, 0);
            if (!QueryResultRow.class.equals(ReflectUtils.getRawTypeFor(optionalType))) {
                throw new DefinitionImportException(format("Unexpected Optional parameter type: %s", optionalType), method);
            }
            final Type queryResultRowType = getGenericTypeArgument(optionalType, 0);
            if (!entityClass.equals(ReflectUtils.getRawTypeFor(queryResultRowType))) {
                throw new DefinitionImportException(format("Unexpected QueryResultRow parameter type: %s", queryResultRowType), method);
            }
            return EntityQueryMethodType.OPTIONAL_ROW;
        }
        // Otherwise ...
        throw new DefinitionImportException(format("Unexpected method return type: %s", returnType), method);
    }

    private static EntityQueryType parseEntityQueryType(EntityQueryMethodType methodType) {
        if (methodType == EntityQueryMethodType.LIST) {
            return EntityQueryType.LIST;
        }
        if (methodType == EntityQueryMethodType.ROW) {
            return EntityQueryType.SCALAR_NON_NULL;
        }
        if (methodType == EntityQueryMethodType.OPTIONAL_ROW) {
            return EntityQueryType.SCALAR_NULL_ALLOWED;
        }
        throw new UnreachableException();
    }

    private static EntityQueryParameterMetadata processQueryParameter(Parameter parameter, int index) {
        requireNonNull(parameter, "parameter");

        GrappleParameter grappleParameter = ReflectUtils.searchParameterAnnotation((Method) parameter.getDeclaringExecutable(), index, GrappleParameter.class).orElse(null);
        // Skip ignored parameters
        if (grappleParameter != null && grappleParameter.ignore()) {
            return null;
        }

        final Type parameterType = parameter.getParameterizedType();

        final EntityQueryParameterMetadata parameterMetadata = new EntityQueryParameterMetadata();
        parameterMetadata.name = (grappleParameter != null && isNotEmpty(grappleParameter.value()) ? grappleParameter.value() : parameter.getName());
        parameterMetadata.index = index;
        parameterMetadata.type = parameterType;
        parameterMetadata.description = (grappleParameter != null && isNotEmpty(grappleParameter.description()) ? grappleParameter.description() : null);

        if (grappleParameter != null && isNotEmpty(grappleParameter.deprecated())) {
            parameterMetadata.deprecationReason = grappleParameter.deprecated();
        }
        else if (parameter.isAnnotationPresent(Deprecated.class)) {
            parameterMetadata.deprecationReason = "Deprecated";
        }

        parameterMetadata.required = ((parameterType instanceof Class<?>) && ((Class<?>) parameterType).isPrimitive()) || (grappleParameter != null && grappleParameter.required());
        return parameterMetadata;
    }
}
