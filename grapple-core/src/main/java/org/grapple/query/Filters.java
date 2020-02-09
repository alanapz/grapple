package org.grapple.query;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.jooq.lambda.Seq.of;
import static org.jooq.lambda.Seq.seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;
import org.grapple.utils.QuickFilterBy;

public final class Filters {

    private Filters() {

    }

    public static <X> EntityFilter<X> alwaysTrue() {
        return new EntityFilter<X>() {

            @Override
            public boolean isAlwaysTrue() {
                return true;
            }

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.alwaysTrue();
            }

            @Override
            public String toString() {
                return "true";
            }
        };
    }

    public static <X> EntityFilter<X> alwaysFalse() {
        return new EntityFilter<X>() {

            @Override
            public boolean isAlwaysFalse() {
                return true;
            }

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.alwaysFalse();
            }

            @Override
            public String toString() {
                return "false";
            }
        };
    }

    public static <X> EntityFilter<X> not(EntityFilter<X> source) {
        requireNonNull(source, "source");
        if (source.isAlwaysTrue()) {
            return alwaysFalse();
        }
        if (source.isAlwaysFalse()) {
            return alwaysTrue();
        }
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.not(source.apply(ctx, queryBuilder));
            }

            @Override
            public String toString() {
                return format("!(%s)", source);
            }
        };
    }

    public static <X> EntityFilter<X> isTrue(QueryField<X, Boolean> selection) {
        return isEqual(selection, true);
    }

    public static <X> EntityFilter<X> isTrue(SingularAttribute<X, Boolean> attribute) {
        return isEqual(attribute, true);
    }

    public static <X> EntityFilter<X> isFalse(QueryField<X, Boolean> selection) {
        return isEqual(selection, false);
    }

    public static <X> EntityFilter<X> isFalse(SingularAttribute<X, Boolean> attribute) {
        return isEqual(attribute, false);
    }

    public static <X, T> EntityFilter<X> isEqual(QueryField<X, T> selection, T value) {
        requireNonNull(selection, "selection");
        if (value == null) {
            return isNull(selection);
        }
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.equal(ctx.get(selection), value);
            }

            @Override
            public String toString() {
                if (Objects.equals(value, Boolean.TRUE)) {
                    return format("%s", selection.getName());
                }
                if (Objects.equals(value, Boolean.FALSE)) {
                    return format("!%s", selection.getName());
                }
                return format("%s == %s", selection.getName(), value);
            }
        };
    }

    public static <X, T> EntityFilter<X> isEqual(SingularAttribute<X, T> attribute, T value) {
        requireNonNull(attribute, "attribute");
        if (value == null) {
            return isNull(attribute);
        }
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.equal(ctx.get(attribute), value);
            }

            @Override
            public String toString() {
                if (Objects.equals(value, Boolean.TRUE)) {
                    return format("%s", attribute.getName());
                }
                if (Objects.equals(value, Boolean.FALSE)) {
                    return format("!%s", attribute.getName());
                }
                return format("%s == %s", attribute.getName(), value);
            }
        };
    }

    public static <X, T> EntityFilter<X> isNotEqual(QueryField<X, T> selection, T value) {
        requireNonNull(selection, "selection");
        if (value == null) {
            return isNotNull(selection);
        }
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.notEqual(ctx.get(selection), value);
            }

            @Override
            public String toString() {
                return format("%s != %s", selection.getName(), value);
            }
        };
    }

    public static <X, T> EntityFilter<X> isNotEqual(SingularAttribute<X, T> attribute, T value) {
        requireNonNull(attribute, "attribute");
        if (value == null) {
            return isNotNull(attribute);
        }
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.notEqual(ctx.get(attribute), value);
            }

            @Override
            public String toString() {
                return format("%s != %s", attribute.getName(), value);
            }
        };
    }

    public static <X> EntityFilter<X> isNull() {
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.isNull(ctx.getEntity());
            }

            @Override
            public String toString() {
                return "IS NULL";
            }
        };
    }

    public static <X, T> EntityFilter<X> isNull(QueryField<X, T> selection) {
        requireNonNull(selection, "selection");
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.isNull(ctx.get(selection));
            }

            @Override
            public String toString() {
                return format("%s IS NULL", selection.getName());
            }
        };
    }

    public static <X, Y> EntityFilter<X> isNull(EntityJoin<X, Y> join) {
        requireNonNull(join, "join");
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.isNull(ctx.join(join).getEntity());
            }

            @Override
            public String toString() {
                return format("%s IS NULL", join.getName());
            }
        };
    }

    public static <X, T> EntityFilter<X> isNull(SingularAttribute<X, T> attribute) {
        requireNonNull(attribute, "attribute");
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.isNull(ctx.get(attribute));
            }

            @Override
            public String toString() {
                return format("%s IS NULL", attribute.getName());
            }
        };
    }

    public static <X> EntityFilter<X> isNotNull() {
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.isNotNull(ctx.getEntity());
            }

            @Override
            public String toString() {
                return "IS NOT NULL";
            }
        };
    }

    public static <X, T> EntityFilter<X> isNotNull(QueryField<X, T> selection) {
        requireNonNull(selection, "selection");
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.isNotNull(ctx.get(selection));
            }

            @Override
            public String toString() {
                return format("%s IS NOT NULL", selection.getName());
            }
        };
    }

    public static <X, Y> EntityFilter<X> isNotNull(EntityJoin<X, Y> join) {
        requireNonNull(join, "join");
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.isNotNull(ctx.join(join).getEntity());
            }

            @Override
            public String toString() {
                return format("%s IS NOT NULL", join.getName());
            }
        };
    }

    public static <X, Y> EntityFilter<X> isNotNull(SingularAttribute<X, Y> attribute) {
        requireNonNull(attribute, "attribute");
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.isNotNull(ctx.get(attribute));
            }

            @Override
            public String toString() {
                return format("%s IS NOT NULL", attribute.getName());
            }
        };
    }

    public static <X, T> EntityFilter<X> contains(QueryField<X, T> selection, Set<T> values) {
        return in(selection, values);
    }

    public static <X, T> EntityFilter<X> in(QueryField<X, T> selection, Set<T> values) {
        requireNonNull(selection, "selection");
        if (values == null) {
            return alwaysTrue();
        }
        if (values.isEmpty()) {
            return alwaysFalse();
        }
        if (values.size() == 1) {
            return isEqual(selection, values.iterator().next());
        }
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.in(ctx.get(selection), values);
            }

            @Override
            public String toString() {
                return format("%s IN (%s)", selection.getName(), of(values).toString(","));
            }
        };
    }

    public static <X, T> EntityFilter<X> in(SingularAttribute<X, T> attribute, Set<T> values) {
        requireNonNull(attribute, "attribute");
        if (values == null) {
            return alwaysTrue();
        }
        if (values.isEmpty()) {
            return alwaysFalse();
        }
        if (values.size() == 1) {
            return isEqual(attribute, values.iterator().next());
        }
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.in(ctx.get(attribute), values);
            }

            @Override
            public String toString() {
                return format("%s IN (%s)", attribute.getName(), of(values).toString(","));
            }
        };
    }

    public static <X> EntityFilter<X> like(QueryField<X, String> selection, String value) {
        requireNonNull(selection, "selection");
        if (value == null) {
            return alwaysTrue();
        }
        if (value.trim().isEmpty()) {
            return alwaysFalse();
        }
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.like(ctx.get(selection), value);
            }

            @Override
            public String toString() {
                return format("%s LIKE %s", selection.getName(), value);
            }
        };
    }

    public static <X> EntityFilter<X> likeCaseInsensitive(QueryField<X, String> selection, String value) {
        requireNonNull(selection, "selection");
        if (value == null) {
            return alwaysTrue();
        }
        if (value.trim().isEmpty()) {
            return alwaysFalse();
        }
        final String valueUppercase = value.trim().toUpperCase();
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.like(queryBuilder.upper(ctx.get(selection)), valueUppercase);
            }

            @Override
            public String toString() {
                return format("%s ILIKE %s", selection.getName(), valueUppercase);
            }
        };
    }

    public static <X, T extends Comparable<T>> EntityFilter<X> lessThan(QueryField<X, T> selection, T value) {
        requireNonNull(selection, "selection");
        if (value == null) {
            return alwaysTrue();
        }
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.lessThan(ctx.get(selection), value);
            }

            @Override
            public String toString() {
                return format("%s < %s", selection.getName(), value);
            }
        };
    }

    public static <X, T extends Comparable<? super T>> EntityFilter<X> lessThanOrEqualTo(QueryField<X, T> selection, T value) {
        requireNonNull(selection, "selection");
        if (value == null) {
            return alwaysTrue();
        }
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.lessThanOrEqualTo(ctx.get(selection), value);
            }

            @Override
            public String toString() {
                return format("%s <= %s", selection.getName(), value);
            }
        };
    }

    public static <X, T extends Comparable<T>> EntityFilter<X> greaterThan(QueryField<X, T> selection, T value) {
        requireNonNull(selection, "selection");
        if (value == null) {
            return alwaysTrue();
        }
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.greaterThan(ctx.get(selection), value);
            }

            @Override
            public String toString() {
                return format("%s > %s", selection.getName(), value);
            }
        };
    }

    public static <X, T extends Comparable<? super T>> EntityFilter<X> greaterThanOrEqualTo(QueryField<X, T> selection, T value) {
        requireNonNull(selection, "selection");
        if (value == null) {
            return alwaysTrue();
        }
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return queryBuilder.greaterThanOrEqualTo(ctx.get(selection), value);
            }

            @Override
            public String toString() {
                return format("%s >= %s", selection.getName(), value);
            }
        };
    }

    public static <X, Y> EntityFilter<X> rebase(Function<EntityContext<X>, EntityContext<Y>> ctxMapper, EntityFilter<Y> filter) {
        requireNonNull(ctxMapper, "ctxMapper");
        requireNonNull(filter, "filter");
        return new EntityFilter<X>() {

            @Override
            public boolean isAlwaysTrue() {
                return filter.isAlwaysTrue();
            }

            @Override
            public boolean isAlwaysFalse() {
                return filter.isAlwaysFalse();
            }

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return filter.apply(ctxMapper.apply(ctx), queryBuilder);
            }

            @Override
            public String toString() {
                return filter.toString();
            }
        };
    }

    public static <X, Y> EntityFilter<X> rebase(EntityJoin<X, Y> join, EntityFilter<Y> filter) {
        requireNonNull(join, "join");
        requireNonNull(filter, "filter");
        return rebase(ctx -> ctx.join(join), filter);
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <X> EntityFilter<X> or(EntityFilter<X>... filters) {
        return or(Arrays.asList(filters));
    }

    public static <X> EntityFilter<X> or(List<EntityFilter<X>> filters) {
        requireNonNull(filters);
        if (filters.isEmpty()) {
            return alwaysTrue();
        }
        if (filters.size() == 1) {
            return filters.get(0);
        }
        return new EntityFilter<X>() {

            @Override
            public boolean isAlwaysTrue() {
                // We are true if any filter is true
                return seq(filters).anyMatch(EntityFilter::isAlwaysTrue);
            }

            @Override
            public boolean isAlwaysFalse() {
                // We are false if all filters are false
                return seq(filters).allMatch(EntityFilter::isAlwaysFalse);
            }

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                final List<Predicate> predicates = new ArrayList<>();
                for(EntityFilter<X> filter: filters) {
                    predicates.add(filter.apply(ctx, queryBuilder));
                }
                return queryBuilder.or(predicates);
            }

            @Override
            public String toString() {
                return seq(filters).toString(" || ", "(", ")");
            }
        };
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <X> EntityFilter<X> and(EntityFilter<X>... filters) {
        return and(Arrays.asList(filters));
    }

    public static <X> EntityFilter<X> and(List<EntityFilter<X>> filters) {
        requireNonNull(filters);
        if (filters.isEmpty()) {
            return alwaysTrue();
        }
        if (filters.size() == 1) {
            return filters.get(0);
        }
        return new EntityFilter<X>() {

            @Override
            public boolean isAlwaysTrue() {
                // We are true if all filters are true
                return seq(filters).allMatch(EntityFilter::isAlwaysTrue);
            }

            @Override
            public boolean isAlwaysFalse() {
                // We are false if any filter is false
                return seq(filters).anyMatch(EntityFilter::isAlwaysFalse);
            }

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                final List<Predicate> predicates = new ArrayList<>();
                for(EntityFilter<X> filter: filters) {
                    predicates.add(filter.apply(ctx, queryBuilder));
                }
                return queryBuilder.and(predicates);
            }

            @Override
            public String toString() {
                return seq(filters).toString(" && ", "(", ")");
            }
        };
    }
    public static <X> EntityFilter<X> quickFilterBy(Collection<? extends QuickFilterBy<X>> quickFilters, String value) {
        if (quickFilters == null || quickFilters.isEmpty()) {
            return alwaysTrue();
        }
        if (value == null || value.trim().isEmpty()) {
            return alwaysTrue();
        }
        return (ctx, queryBuilder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            for (QuickFilterBy<X> quickFilter: quickFilters) {
                predicates.add(queryBuilder.likeNonAnchored(quickFilter.getPath(ctx), value.trim()));
            }
            return queryBuilder.or(predicates);
        };
    }
}
