package org.grapple.schema.impl;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.reifyMap;
import static org.jooq.lambda.Seq.seq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import org.grapple.query.EntityField;
import org.grapple.query.EntityFilter;
import org.grapple.query.EntityJoin;
import org.grapple.query.FetchSet;
import org.grapple.query.Filters;
import org.grapple.query.QueryField;
import org.grapple.query.QueryResultList;
import org.grapple.query.QueryResultRow;
import org.grapple.query.RootFetchSet;
import org.grapple.query.SortDirection;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityFilterItemResolver;
import org.grapple.schema.EntityQueryResolver;
import org.grapple.schema.EntityQueryType;
import org.grapple.utils.UnexpectedException;

final class RuntimeWiring {

    interface EntitySelectionWiring<X> {

        Class<X> getEntityClass();

        String getFieldName();

        void addSelection(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Field field);

        Object resolveResponse(SchemaBuilderContext ctx, DataFetchingEnvironment environment, QueryResultRow<X> resultRow, Field field);
    }

    static <X, T> EntitySelectionWiring<X> entitySelectionFieldWiring(Class<X> entityClass, String fieldName, EntityField<X, T> entityField) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(fieldName, "fieldName");
        requireNonNull(entityField, "entityField");
        return new EntitySelectionWiring<X>() {

            @Override
            public Class<X> getEntityClass() {
                return entityClass;
            }

            @Override
            public String getFieldName() {
                return fieldName;
            }

            @Override
            public void addSelection(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Field field) {
                requireNonNull(fetchSet, "fetchSet");
                requireNonNull(field, "field");
                fetchSet.select(entityField);
            }

            @Override
            public Object resolveResponse(SchemaBuilderContext ctx, DataFetchingEnvironment environment, QueryResultRow<X> resultRow, Field field) {
                requireNonNull(resultRow, "resultRow");
                requireNonNull(field, "field");
                return resultRow.get(entityField);
            }
        };
    }

    static <X, Y> EntitySelectionWiring<X> entitySelectionJoinWiring(Class<X> entityClass, String fieldName, Class<Y> joinedEntityClass, EntityJoin<X, Y> entityJoin) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(fieldName, "fieldName");
        requireNonNull(joinedEntityClass, "joinedEntityClass");
        requireNonNull(entityJoin, "entityJoin");
        return new EntitySelectionWiring<X>() {

            @Override
            public Class<X> getEntityClass() {
                return entityClass;
            }

            @Override
            public String getFieldName() {
                return fieldName;
            }

            @Override
            public void addSelection(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Field field) {
                requireNonNull(fetchSet, "fetchSet");
                requireNonNull(field, "field");
                ctx.applyEntitySelection(environment, joinedEntityClass, fetchSet.join(entityJoin), field.getSelectionSet());
            }

            @Override
            public Object resolveResponse(SchemaBuilderContext ctx, DataFetchingEnvironment environment, QueryResultRow<X> resultRow, Field field) {
                requireNonNull(resultRow, "resultRow");
                requireNonNull(field, "field");
                if (field.getSelectionSet() == null) {
                    throw new UnexpectedException(format("Expected selection set for :%s", field));
                }
                final QueryResultRow<Y> joinedResultRow = resultRow.getJoin(entityJoin);
                if (!joinedResultRow.isExists()) {
                    return null;
                }
                return ctx.parseQueryResponse(environment, joinedEntityClass, field.getSelectionSet(), joinedResultRow);
            }
        };
    }

    interface EntityFilterWiring<X> {

        Class<X> getEntityClass();

        String getFieldName();

        EntityFilter<X> resolveFilter(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Object rawArgs);
    }

    static <X, T> EntityFilterWiring<X> entityFilterCustomWiring(Class<X> entityClass, String fieldName, TypeLiteral<T> fieldType, EntityFilterItemResolver<X, T> resolver) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(fieldName, "fieldName");
        requireNonNull(fieldType, "fieldType");
        requireNonNull(resolver, "resolver");
        return new EntityFilterWiring<X>() {

            @Override
            public Class<X> getEntityClass() {
                return entityClass;
            }

            @Override
            public String getFieldName() {
                return fieldName;
            }

            @Override
            public EntityFilter<X> resolveFilter(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Object rawArgs) {
                requireNonNull(fetchSet, "fetchSet");
                requireNonNull(rawArgs, "rawArgs");
                return resolver.get(environment, fetchSet, ctx.convertInput(fieldType, rawArgs));
            }

        };
    }

    static <X, T> EntityFilterWiring<X> entityFilterFieldWiring(Class<X> entityClass, String fieldName, TypeLiteral<T> fieldFilterType, QueryField<X, T> queryField) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(fieldName, "fieldName");
        requireNonNull(fieldFilterType, "fieldFilterType");
        requireNonNull(queryField, "queryField");
        return new EntityFilterWiring<X>() {

            @Override
            public Class<X> getEntityClass() {
                return entityClass;
            }

            @Override
            public String getFieldName() {
                return fieldName;
            }

            @Override
            public EntityFilter<X> resolveFilter(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Object rawArgs) {
                requireNonNull(fetchSet, "fetchSet");
                requireNonNull(rawArgs, "rawArgs");
                final Map<String, Object> args = reifyMap((Map<?, ?>) rawArgs);
                return ctx.generateFieldFilter(environment, fieldFilterType, queryField, args);
            }
        };
    }

    static <X, Y> EntityFilterWiring<X> entityFilterJoinWiring(Class<X> entityClass, String fieldName, Class<Y> joinedEntityClass, EntityJoin<X, Y> entityJoin) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(fieldName, "fieldName");
        requireNonNull(joinedEntityClass, "joinedEntityClass");
        requireNonNull(entityJoin, "entityJoin");
        return new EntityFilterWiring<X>() {

            @Override
            public Class<X> getEntityClass() {
                return entityClass;
            }

            @Override
            public String getFieldName() {
                return fieldName;
            }

            @Override
            public EntityFilter<X> resolveFilter(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Object rawArgs) {
                requireNonNull(fetchSet, "fetchSet");
                requireNonNull(rawArgs, "rawArgs");
                final Map<String, Object> args = reifyMap((Map<?, ?>) rawArgs);
                return Filters.rebase(entityJoin, ctx.generateEntityFilter(environment, joinedEntityClass, fetchSet.join(entityJoin), args));
            }
        };
    }

    static <X> EntityFilterWiring<X> entityFilterNotWiring(Class<X> entityClass, String fieldName) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(fieldName, "fieldName");
        return new EntityFilterWiring<X>() {

            @Override
            public Class<X> getEntityClass() {
                return entityClass;
            }

            @Override
            public String getFieldName() {
                return fieldName;
            }

            @Override
            public EntityFilter<X> resolveFilter(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Object rawArgs) {
                requireNonNull(fetchSet, "fetchSet");
                requireNonNull(rawArgs, "rawArgs");
                final List<EntityFilter<X>> filters = new ArrayList<>();
                for (Map<String, Object> arg: seq((Collection<?>) rawArgs).map(x -> (Map<?, ?>) x).map(reifyMap())) {
                    filters.add(Filters.not(ctx.generateEntityFilter(environment, entityClass, fetchSet, arg)));
                }
                return Filters.and(filters);
            }
        };
    }

    static <X> EntityFilterWiring<X> entityFilterAndWiring(Class<X> entityClass, String fieldName) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(fieldName, "fieldName");
        return new EntityFilterWiring<X>() {

            @Override
            public Class<X> getEntityClass() {
                return entityClass;
            }

            @Override
            public String getFieldName() {
                return fieldName;
            }

            @Override
            public EntityFilter<X> resolveFilter(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Object rawArgs) {
                requireNonNull(fetchSet, "fetchSet");
                requireNonNull(rawArgs, "rawArgs");
                final List<EntityFilter<X>> filters = new ArrayList<>();
                for (Map<String, Object> arg: seq((Collection<?>) rawArgs).map(x -> ((Map<?, ?>) x)).map(reifyMap())) {
                    filters.add(ctx.generateEntityFilter(environment, entityClass, fetchSet, arg));
                }
                return Filters.and(filters);
            }
        };
    }

    static <X> EntityFilterWiring<X> entityFilterOrWiring(Class<X> entityClass, String fieldName) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(fieldName, "fieldName");
        return new EntityFilterWiring<X>() {

            @Override
            public Class<X> getEntityClass() {
                return entityClass;
            }

            @Override
            public String getFieldName() {
                return fieldName;
            }

            @Override
            public EntityFilter<X> resolveFilter(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Object rawArgs) {
                requireNonNull(fetchSet, "fetchSet");
                requireNonNull(rawArgs, "rawArgs");
                final List<EntityFilter<X>> filters = new ArrayList<>();
                for (Map<String, Object> arg: seq((Collection<?>) rawArgs).map(x -> ((Map<?, ?>) x)).map(reifyMap())) {
                    filters.add(ctx.generateEntityFilter(environment, entityClass, fetchSet, arg));
                }
                return Filters.or(filters);
            }
        };
    }

    interface EntityQueryWiring<X> {

        Class<X> getEntityClass();

        String getQueryName();

        EntityQueryType getQueryType();

        QueryResultList<X> executeQuery(SchemaBuilderContext ctx, DataFetchingEnvironment environment, RootFetchSet<X> fetchSet, Map<String, Object> args);
    }

    static <X> EntityQueryWiring<X> entityQueryWiring(Class<X> entityClass, String queryName, EntityQueryType queryType, EntityQueryResolver<X> queryResolver) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(queryName, "queryName");
        requireNonNull(queryType, "queryType");
        requireNonNull(queryResolver, "queryParameters");
        return new EntityQueryWiring<X>() {

            @Override
            public Class<X> getEntityClass() {
                return entityClass;
            }

            @Override
            public String getQueryName() {
                return queryName;
            }

            @Override
            public EntityQueryType getQueryType() {
                return queryType;
            }

            @Override
            public QueryResultList<X> executeQuery(SchemaBuilderContext ctx, DataFetchingEnvironment environment, RootFetchSet<X> fetchSet, Map<String, Object> queryParameters) {
                requireNonNull(fetchSet, "fetchSet");
                requireNonNull(queryParameters, "queryParameters");
                return queryResolver.execute(environment, fetchSet, queryParameters);
            }
        };
    }

    interface EntityQueryParameterWiring<T> {

        String getQueryName();

        String getParameterName();

        TypeLiteral<T> getParameterType();

    }

    static <T> EntityQueryParameterWiring<T> entityQueryParameterWiring(String queryName, String parameterName, TypeLiteral<T> parameterType) {
        requireNonNull(queryName, "queryName");
        requireNonNull(parameterName, "parameterName");
        requireNonNull(parameterType, "parameterType");
        return new EntityQueryParameterWiring<T>() {

            @Override
            public String getQueryName() {
                return queryName;
            }

            @Override
            public String getParameterName() {
                return parameterName;
            }

            @Override
            public TypeLiteral<T> getParameterType() {
                return parameterType;
            }
        };
    }

    @FunctionalInterface
    public interface FieldFilterCallback<T> {

        EntityFilter<?> get(DataFetchingEnvironment environment, QueryField<?, T> queryField, Object args);

    }

    static final class FieldFilterWiring<T> {

        private final TypeLiteral<T> fieldType;

        private final String name;

        private final FieldFilterCallback<T> callback;

        private FieldFilterWiring(TypeLiteral<T> fieldType, String name, FieldFilterCallback<T> callback) {
            this.fieldType = requireNonNull(fieldType, "fieldType");
            this.name = requireNonNull(name, "name");
            this.callback = requireNonNull(callback, "callback");
        }

        TypeLiteral<T> getFieldType() {
            return fieldType;
        }

        String getName() {
            return name;
        }

        @SuppressWarnings("unchecked")
        <X> EntityFilter<X> resolveFilter(DataFetchingEnvironment environment, QueryField<X, T> queryField, Object args) {
            requireNonNull(environment, "environment");
            requireNonNull(queryField, "queryField");
            requireNonNull(args, "args");
            return (EntityFilter<X>) callback.get(environment, queryField, args);
        }

        static <T> FieldFilterWiring<T> fieldFilterWiring(TypeLiteral<T> fieldType, String name, FieldFilterCallback<T> callback) {
            requireNonNull(fieldType, "fieldType");
            requireNonNull(name, "name");
            requireNonNull(callback, "callback");
            return new FieldFilterWiring<>(fieldType, name, callback);
        }
    }

    interface EntityOrderByWiring<X> {

        Class<X> getEntityClass();

        String getFieldName();

        void applyOrderBy(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Object args);
    }

    static <X, T> EntityOrderByWiring<X> entityOrderByFieldWiring(Class<X> entityClass, String fieldName, QueryField<X, T> queryField) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(fieldName, "fieldName");
        requireNonNull(queryField, "queryField");
        return new EntityOrderByWiring<X>() {

            @Override
            public Class<X> getEntityClass() {
                return entityClass;
            }

            @Override
            public String getFieldName() {
                return fieldName;
            }

            @Override
            public void applyOrderBy(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Object rawArgs) {
                requireNonNull(fetchSet, "fetchSet");
                requireNonNull(rawArgs, "rawArgs");
                fetchSet.orderBy(queryField, (SortDirection) rawArgs);
            }
        };
    }

    static <X, Y> EntityOrderByWiring<X> entityOrderByJoinWiring(Class<X> entityClass, Class<Y> joinedEntityClass, String fieldName, EntityJoin<X, Y> entityJoin) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(joinedEntityClass, "joinedEntityClass");
        requireNonNull(fieldName, "fieldName");
        requireNonNull(entityJoin, "entityJoin");
        return new EntityOrderByWiring<X>() {

            @Override
            public Class<X> getEntityClass() {
                return entityClass;
            }

            @Override
            public String getFieldName() {
                return fieldName;
            }

            @Override
            public void applyOrderBy(SchemaBuilderContext ctx, DataFetchingEnvironment environment, FetchSet<X> fetchSet, Object rawArgs) {
                requireNonNull(fetchSet, "fetchSet");
                requireNonNull(rawArgs, "rawArgs");
                final Map<String, Object> args = reifyMap((Map<?, ?>) rawArgs);
                ctx.applyEntityOrderBy(environment, joinedEntityClass, fetchSet.join(entityJoin), args);
            }
        };
    }
}
