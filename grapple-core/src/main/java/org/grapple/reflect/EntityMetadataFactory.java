package org.grapple.reflect;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.query.Filters.alwaysTrue;
import static org.grapple.reflect.ClassLiteral.classLiteral;
import static org.grapple.reflect.ReflectUtils.searchMethodAnnotation;
import static org.grapple.reflect.ReflectUtils.typeLiteral;
import static org.grapple.reflect.ReflectUtils.wrapPrimitiveTypeIfNecessary;
import static org.grapple.utils.Utils.isNotEmpty;
import static org.grapple.utils.Utils.toSet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import graphql.schema.DataFetchingEnvironment;
import org.grapple.invoker.GrappleFilter;
import org.grapple.metadata.DeprecationReason;
import org.grapple.metadata.FieldNotExported;
import org.grapple.query.EntityFilter;
import org.grapple.query.FetchSet;
import org.grapple.schema.DefinitionImportException;
import org.grapple.schema.QueryExecutionException;
import org.grapple.utils.UnexpectedException;

public final class EntityMetadataFactory {

    private EntityMetadataFactory() {

    }

    public static <X, T> EntityFilterMethodMetadata<X, T> parseFilterMethod(Method method) {
        requireNonNull(method, "method");

        if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers()) || Modifier.isAbstract(method.getModifiers())) {
            return null;
        }
        if (!toSet(0, 1).contains(method.getParameterCount())) {
            return null;
        }
        if (!EntityFilter.class.isAssignableFrom(method.getReturnType())) {
            return null;
        }
        if (searchMethodAnnotation(method, FieldNotExported.class).isPresent()) {
            return null;
        }
        if (!searchMethodAnnotation(method, GrappleFilter.class).isPresent()) {
            return null;
        }

        final GrappleFilter grappleFilter = searchMethodAnnotation(method, GrappleFilter.class).orElse(null);
        if (grappleFilter != null && grappleFilter.ignore()) {
            return null;
        }

        // Make sure filter returns entity of type EntityFilter<X>
        final Type entityClass = ReflectUtils.getGenericTypeArgument(method.getGenericReturnType(), 0);
        if (!(entityClass instanceof Class<?>)) {
            throw new DefinitionImportException(format("Unexpected EntityFilter return type: %s", entityClass), method);
        }

        final boolean isUnaryFilter = (method.getParameterCount() == 0);

        return new EntityFilterMethodMetadata<X, T>(){

            @Override
            @SuppressWarnings("unchecked")
            public Class<X> getEntityClass() {
                return (Class<X>) entityClass;
            }

            @Override
            public Method getMethod() {
                return method;
            }

            @Override
            public String getFilterName() {
                return (grappleFilter != null && isNotEmpty(grappleFilter.value()) ? grappleFilter.value() : method.getName());
            }

            @Override
            public String getDescription() {
                return (grappleFilter != null && isNotEmpty(grappleFilter.description()) ? grappleFilter.description() : null);
            }

            @Override
            public String getDeprecationReason() {
                if (grappleFilter != null && isNotEmpty(grappleFilter.deprecated())) {
                    return grappleFilter.deprecated();
                }
                final DeprecationReason deprecationReason = searchMethodAnnotation(method, DeprecationReason.class).orElse(null);
                if (deprecationReason != null && isNotEmpty(deprecationReason.value())) {
                    return deprecationReason.value();
                }
                if (searchMethodAnnotation(method, Deprecated.class).isPresent()) {
                    return "Deprecated";
                }
                return null;
            }

            @Override
            @SuppressWarnings("unchecked")
            public TypeLiteral<T> getParameterType() {
                return (isUnaryFilter ? (TypeLiteral<T>) classLiteral(Boolean.class) : typeLiteral(method.getGenericParameterTypes()[0]));
            }

            @Override
            public EntityFilter<X> generate(DataFetchingEnvironment env, FetchSet<X> fetchSet, T args) {
                requireNonNull(args, "args");
                try {
                    return (isUnaryFilter ? generateNoParameterFilter(args) : generateParameterFilter(args));
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (InvocationTargetException e) {
                    throw (e.getCause() instanceof RuntimeException ? (RuntimeException) e.getCause() : new QueryExecutionException(method, e.getCause()));
                }
                catch (Exception e) {
                    throw new QueryExecutionException(method, e);
                }
            }

            private EntityFilter<X> generateNoParameterFilter(Object args) throws Exception {
                if (!(args instanceof Boolean)) {
                    throw new UnexpectedException(format("Unexpected parameter type for: %s", method));
                }
                if (!((boolean) args)) {
                    return alwaysTrue();
                }
                final @SuppressWarnings("unchecked") EntityFilter<X> result = (EntityFilter<X>) method.invoke(null); // Static, no object, no parameters
                return result;
            }

            private EntityFilter<X> generateParameterFilter(Object args) throws Exception {
                final Class<?> parameterType = method.getParameterTypes()[0];
                if (!wrapPrimitiveTypeIfNecessary(parameterType).isInstance(args)) { // wrapPrimitiveType to handle Integer -> int conversions
                    throw new TypeConversionException(format("Couldn't convert %s (of type %s) to %s for method: %s", args, args.getClass().getName(), parameterType.getName(), method.getName()));
                }
                final @SuppressWarnings("unchecked") EntityFilter<X> result = (EntityFilter<X>) method.invoke(null, args); // Static, parameters
                return result;
            }
        };
    }
}
