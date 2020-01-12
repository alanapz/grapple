package org.grapple.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;
import org.grapple.utils.QuickFilterBy;
import org.jooq.lambda.Seq;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.jooq.lambda.Seq.seq;

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
            public Predicate apply(EntityContext<X> ctx, QueryBuilder builder) {
                return builder.alwaysTrue();
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
            public Predicate apply(EntityContext<X> ctx, QueryBuilder builder) {
                return builder.alwaysTrue();
            }

            @Override
            public String toString() {
                return "false";
            }
        };
    }

    public static <X> EntityFilter<X> isTrue(QueryField<X, Boolean> selection) {
        return isEqual(selection, true);
    }

    public static <X> EntityFilter<X> isFalse(QueryField<X, Boolean> selection) {
        return isEqual(selection, false);
    }

    public static <X, T> EntityFilter<X> isEqual(QueryField<X, T> selection, T value) {
        requireNonNull(selection, "selection");
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder builder) {
                return builder.equal(ctx.get(selection), value);
            }

            @Override
            public String toString() {
                return format("%s=%s", selection.getName(), value);
            }
        };
    }

    public static <X, T> EntityFilter<X> isNotEqual(QueryField<X, T> selection, T value) {
        requireNonNull(selection, "selection");
        return (ctx, builder) -> builder.notEqual(ctx.get(selection), value);
    }

    public static <X> EntityFilter<X> isTrue(SingularAttribute<X, Boolean> attribute) {
        return isEqual(attribute, true);
    }

    public static <X> EntityFilter<X> isFalse(SingularAttribute<X, Boolean> attribute) {
        return isEqual(attribute, false);
    }

    public static <X, T> EntityFilter<X> isEqual(SingularAttribute<X, T> attribute, T value) {
        requireNonNull(attribute, "attribute");
        return (ctx, builder) -> builder.equal(ctx.get(attribute), value);
    }

    public static <X, T> EntityFilter<X> isNotEqual(SingularAttribute<X, T> attribute, T value) {
        requireNonNull(attribute, "attribute");
        return (ctx, builder) -> builder.notEqual(ctx.get(attribute), value);
    }

    public static <X, T> EntityFilter<X> contains(QueryField<X, T> selection, Set<T> values) {
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
            public Predicate apply(EntityContext<X> ctx, QueryBuilder builder) {
                return builder.in(ctx.get(selection), values);
            }

            @Override
            public String toString() {
                return format("%s IN (%s)", selection.getName(), Seq.of(values).toString(","));
            }
        };
    }

    public static <X> EntityFilter<X> not(EntityFilter<X> source) {
        requireNonNull(source, "source");
        return new EntityFilter<X>() {

            @Override
            public Predicate apply(EntityContext<X> ctx, QueryBuilder builder) {
                return builder.not(source.apply(ctx, builder));
            }

            @Override
            public String toString() {
                return format("!(%s)", source);
            }
        };
    }

    @SafeVarargs
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
            public Predicate apply(EntityContext<X> ctx, QueryBuilder builder) {
                final List<Predicate> predicates = new ArrayList<>();
                for(EntityFilter<X> filter: filters) {
                    predicates.add(filter.apply(ctx, builder));
                }
                return builder.or(predicates);
            }

            @Override
            public String toString() {
                return seq(filters).toString(" || ", "(", ")");
            }
        };
    }

    @SafeVarargs
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
            public Predicate apply(EntityContext<X> ctx, QueryBuilder builder) {
                final List<Predicate> predicates = new ArrayList<>();
                for(EntityFilter<X> filter: filters) {
                    predicates.add(filter.apply(ctx, builder));
                }
                return builder.and(predicates);
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
        return (ctx, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            for (QuickFilterBy<X> quickFilter: quickFilters) {
                predicates.add(builder.likeNonAnchored(quickFilter.getPath(ctx), value.trim()));
            }
            return builder.or(predicates);
        };
    }
}
