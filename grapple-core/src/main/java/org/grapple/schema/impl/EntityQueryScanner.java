package org.grapple.schema.impl;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.reflect.ReflectUtils.wrapPrimitiveTypeIfNecessary;
import static org.grapple.utils.Utils.coalesce;
import static org.grapple.utils.Utils.isNotEmpty;
import static org.jooq.lambda.Seq.of;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import graphql.schema.DataFetchingEnvironment;
import org.grapple.invoker.GrappleParameter;
import org.grapple.invoker.GrappleQuery;
import org.grapple.query.QueryResultList;
import org.grapple.query.RootFetchSet;
import org.grapple.reflect.ReflectUtils;
import org.grapple.reflect.TypeConversionException;
import org.grapple.schema.DefinitionImportException;
import org.grapple.schema.EntityQueryResolver;
import org.grapple.schema.EntityQueryType;
import org.grapple.schema.QueryExecutionException;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple.utils.NoDuplicatesSet;
import org.grapple.utils.UnexpectedException;

final class EntityQueryScanner {

    private EntityQueryScanner() {

    }

    static Set<QueryMethodResult<?>> processClass(Class<?> clazz) {
        requireNonNull(clazz, "clazz");
        final Set<QueryMethodResult<?>> results = new NoDuplicatesSet<>();
        for (Method method : clazz.getDeclaredMethods()) {
            final QueryMethodResult<?> result = processMethod(method);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }

    private static QueryMethodResult<?> processMethod(Method method) {
        requireNonNull(method, "method");

        final GrappleQuery grappleQuery = ReflectUtils.searchMethodAnnotation(method, GrappleQuery.class);
        // We are only interested in methods annotated with @GrappleQuery
        if (grappleQuery == null) {
            return null;
        }

        // Skip ignored parameters
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

        // Make sure return type is QueryResultList<EntityClass>
        final Type returnType = method.getGenericReturnType();
        if (!QueryResultList.class.equals(ReflectUtils.getRawTypeFor(returnType))) {
            throw new DefinitionImportException(format("Unexpected method return type: %s", returnType), method);
        }
        if (!entityClass.equals(ReflectUtils.getGenericTypeArgument(returnType, 0))) {
            throw new DefinitionImportException("Unexpected QueryResultList parameter type", method);
        }

        final QueryMethodResult<?> result = new QueryMethodResult<>((Class<?>) entityClass);
        result.method = method;
        result.queryName = (isNotEmpty(grappleQuery.value()) ? grappleQuery.value() : method.getName());
        result.queryType = coalesce(grappleQuery.type(), EntityQueryType.LIST);
        result.description = (isNotEmpty(grappleQuery.description()) ? grappleQuery.description() : null);

        if (isNotEmpty(grappleQuery.deprecated())) {
            result.deprecationReason = grappleQuery.deprecated();
        }
        else if (method.isAnnotationPresent(Deprecated.class)) {
            result.deprecationReason = "Deprecated";
        }

        for (int index = 0; index < method.getParameterCount(); index++) {
            final Parameter parameter = method.getParameters()[index];
            if (parameter == fetchSetArgument) {
                result.fetchSetParameterIndex = index;
                continue;
            }
            final QueryParameterResult parameterResult = processParameter(parameter, index);
            if (parameterResult != null) {
                result.parameters.put(parameterResult.name, parameterResult);
            }
        }

        return result;
    }


    private static QueryParameterResult processParameter(Parameter parameter, int index) {
        requireNonNull(parameter, "parameter");

        GrappleParameter grappleParameter = ReflectUtils.searchParameterAnnotation((Method) parameter.getDeclaringExecutable(), index, GrappleParameter.class);
        // Skip ignored parameters
        if (grappleParameter != null && grappleParameter.ignore()) {
            return null;
        }

        final Type parameterType = parameter.getParameterizedType();

        final QueryParameterResult result = new QueryParameterResult();
        result.name = (grappleParameter != null && isNotEmpty(grappleParameter.value()) ? grappleParameter.value() : parameter.getName());
        result.index = index;
        result.type = parameterType;
        result.description = (grappleParameter != null && isNotEmpty(grappleParameter.description()) ? grappleParameter.description() : null);

        if (grappleParameter != null && isNotEmpty(grappleParameter.deprecated())) {
            result.deprecationReason = grappleParameter.deprecated();
        }
        else if (parameter.isAnnotationPresent(Deprecated.class)) {
            result.deprecationReason = "Deprecated";
        }

        result.required = ((parameterType instanceof Class<?>) && ((Class<?>) parameterType).isPrimitive()) || (grappleParameter != null && grappleParameter.required());
        result.typeAlias = (grappleParameter != null && isNotEmpty(grappleParameter.typeAlias()) ? grappleParameter.typeAlias() : null);
        return result;
    }

    public static final class QueryMethodResult<X> {

        public final Class<X> entityClass;

        public Method method;

        public String queryName;

        public EntityQueryType queryType;

        public String description;

        public String deprecationReason;

        public final Map<String, QueryParameterResult> parameters = new NoDuplicatesMap<>();

        public int fetchSetParameterIndex; // Index of fetch set in method params array

        private QueryMethodResult(Class<X> entityClass) {
            this.entityClass = requireNonNull(entityClass, "entityClass");
        }
    }

    public static final class QueryParameterResult {

        public String name;

        public int index;

        public Type type;

        public String description;

        public String deprecationReason;

        public boolean required;

        public String typeAlias;
    }

    static <X> EntityQueryResolver<X> buildQueryResolver(QueryMethodResult<X> methodResult, Object instance) {
        requireNonNull(methodResult, "methodResult");
        requireNonNull(instance, "instance");
        return new EntityQueryResolver<X>() {

            @Override
            @SuppressWarnings("unchecked")
            public QueryResultList<X> execute(DataFetchingEnvironment environment, RootFetchSet<X> fetchSet, Map<String, Object> queryParameters) {
                requireNonNull(environment, "environment");
                requireNonNull(fetchSet, "fetchSet");
                requireNonNull(queryParameters, "queryParameters");

                final Method method = methodResult.method;

                final Object[] invokerParams = new Object[method.getParameterCount()];
                invokerParams[methodResult.fetchSetParameterIndex] = fetchSet;

                queryParameters.forEach((paramName, paramValue) -> {
                    final QueryParameterResult parameterResult = methodResult.parameters.get(paramName);
                    if (parameterResult == null) {
                        throw new UnexpectedException(format("Unknown parameter: %s for method: %s", paramName, method.getName()));
                    }
                    if (paramValue == null) {
                        return;
                    }
                    final Class<?> parameterType = method.getParameterTypes()[parameterResult.index];
                    if (!wrapPrimitiveTypeIfNecessary(parameterType).isInstance(paramValue)) { // wrapPrimitiveType to handle Integer -> int conversions
                        throw new TypeConversionException(format("Couldn't convert %s (of type %s) to %s for method: %s", paramValue, paramValue.getClass().getName(), parameterType.getName(), method.getName()));
                    }
                    invokerParams[parameterResult.index] = paramValue;
                });

                try {
                    return (QueryResultList<X>) method.invoke(instance, invokerParams);
                }
                catch (InvocationTargetException e) {
                    throw new QueryExecutionException(method, e.getCause());
                }
                catch (Exception e) {
                    throw new QueryExecutionException(method, e);
                }
            }

            @Override
            public String toString() {
                return format("%s.%s", methodResult.method.getDeclaringClass().getName(), methodResult.method.getName());
            }
        };
    }

}
