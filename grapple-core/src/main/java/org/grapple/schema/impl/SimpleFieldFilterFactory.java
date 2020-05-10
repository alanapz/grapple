package org.grapple.schema.impl;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLTypeUtil.isEnum;
import static graphql.schema.GraphQLTypeUtil.isScalar;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.Objects.requireNonNull;
import static org.grapple.schema.impl.SimpleFieldFilterItem.simpleFieldFilterItem;

import java.util.Collection;
import java.util.HashSet;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLType;
import org.grapple.query.EntityFilter;
import org.grapple.query.Filters;
import org.grapple.query.QueryField;
import org.grapple.reflect.TypeLiteral;

final class SimpleFieldFilterFactory {

    private SimpleFieldFilterFactory() {

    }

    @SuppressWarnings("unchecked")
    static <T> FieldFilterDefinitionImpl<T> constructDefaultFilter(EntitySchemaImpl schema, TypeLiteral<T> fieldType, GraphQLInputType gqlType) {
        requireNonNull(schema, "schema");
        requireNonNull(fieldType, "fieldType");
        requireNonNull(gqlType, "gqlType");

        final FieldFilterDefinitionImpl<T> filterDefinition = new FieldFilterDefinitionImpl<>(schema, fieldType);

        filterDefinition.addItem(simpleFieldFilterItem("isNull", GraphQLBoolean, (env, queryField, args) -> ((boolean) args) ? Filters.isNull(queryField) : Filters.isNotNull(queryField)));

        if (isScalar(unwrapNonNull(gqlType)) || isEnum(unwrapNonNull(gqlType))) {

            filterDefinition.addItem(simpleFieldFilterItem("is", (GraphQLInputType) unwrapNonNull(gqlType), (env, queryField, args) -> Filters.isEqual(queryField, (T) args)));
            filterDefinition.addItem(simpleFieldFilterItem("isNot", (GraphQLInputType) unwrapNonNull(gqlType), (env, queryField, args) -> Filters.isNotEqual(queryField, (T) args)));
            filterDefinition.addItem(simpleFieldFilterItem("in", GraphQLList.list(unwrapNonNull(gqlType)), (env, queryField, args) -> Filters.contains(queryField, new HashSet<>((Collection<T>) args))));
            filterDefinition.addItem(simpleFieldFilterItem("notIn", GraphQLList.list(unwrapNonNull(gqlType)), (env, queryField, args) -> Filters.not(Filters.contains(queryField, new HashSet<>((Collection<T>) args)))));

            // Numeric types have < <= > >=
            if (fieldType.isSubtypeOf(Comparable.class) && !fieldType.isSubtypeOf(String.class) && !fieldType.isSubtypeOf(Boolean.class) && !fieldType.isSubtypeOf(Enum.class)) {
                filterDefinition.addItem(simpleFieldFilterItem("lt", (GraphQLInputType) unwrapNonNull(gqlType), (env, queryField, args) -> applyUnsafe(queryField, args, Filters::lessThan)));
                filterDefinition.addItem(simpleFieldFilterItem("lte", (GraphQLInputType) unwrapNonNull(gqlType), (env, queryField, args) -> applyUnsafe(queryField, args, Filters::lessThanOrEqualTo)));
                filterDefinition.addItem(simpleFieldFilterItem("gt", (GraphQLInputType) unwrapNonNull(gqlType), (env, queryField, args) -> applyUnsafe(queryField, args, Filters::greaterThan)));
                filterDefinition.addItem(simpleFieldFilterItem("gte", (GraphQLInputType) unwrapNonNull(gqlType), (env, queryField, args) -> applyUnsafe(queryField, args, Filters::greaterThanOrEqualTo)));
            }

            // String types have like, ulike
            if (fieldType.isSubtypeOf(String.class)) {
                filterDefinition.addItem(simpleFieldFilterItem("like", GraphQLString, (env, queryField, args) -> Filters.like((QueryField<Object, String>) queryField, (String) args)));
                filterDefinition.addItem(simpleFieldFilterItem("notLike", GraphQLString, (env, queryField, args) -> Filters.not(Filters.like((QueryField<Object, String>) queryField, (String) args))));
                filterDefinition.addItem(simpleFieldFilterItem("likeCaseInsensitive", GraphQLString, (env, queryField, args) -> Filters.likeCaseInsensitive((QueryField<Object, String>) queryField, (String) args)));
                filterDefinition.addItem(simpleFieldFilterItem("notLikeCaseInsensitive", GraphQLString, (env, queryField, args) -> Filters.not(Filters.likeCaseInsensitive((QueryField<Object, String>) queryField, (String) args))));
            }
        }
        return filterDefinition;
    }

    private interface UnsafeCallback<X, T extends Comparable<T>> {

        EntityFilter<X> build(QueryField<X, T> queryField, T args);

    }

    @SuppressWarnings("unchecked")
    private static <X, T extends Comparable<T>, U> EntityFilter<X> applyUnsafe(QueryField<X, U> queryField, Object args, UnsafeCallback<X, T> callback) {
        return callback.build((QueryField<X, T>) queryField, (T) args);
    }
}
