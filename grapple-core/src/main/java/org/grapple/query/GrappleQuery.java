package org.grapple.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.persistence.EntityManager;
import org.grapple.utils.EntitySortKey;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

public final class GrappleQuery {

    private GrappleQuery() {

    }

    public static <X> RootFetchSet<X> newQuery() {
        return new RootFetchSetImpl<>();
    }

    private static abstract class AbstractFetchSetImpl<X> implements FetchSet<X>  {

        private final Set<EntityField<X, ?>> selections = new LinkedHashSet<>();

        private final Set<EntityFilter<X>> filters = new LinkedHashSet<>();

        private final Map<EntityJoin<X, ?>, FetchSet<?>> joins = new LinkedHashMap<>();

        @Override
        public FetchSet<X> select(EntityField<X, ?> selection) {
            requireNonNull(selection, "selection");
            selections.add(selection);
            return this;
        }

        @Override
        public Set<EntityField<X, ?>> getSelections() {
            return unmodifiableSet(selections);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <Y> FetchSet<X> join(EntityJoin<X, Y> join, Consumer<FetchSet<Y>> consumer) {
            requireNonNull(join, "join");
            final FetchSet<Y> fetchSet = (FetchSet<Y>) joins.computeIfAbsent(join, unused -> new FetchSetImpl<>(fetchRoot(), this, join));
            if (consumer != null) {
                consumer.accept(fetchSet);
            }
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <Y> FetchSet<Y> getJoin(EntityJoin<X, Y> join) {
            requireNonNull(join, "join");
            final FetchSet<Y> fetchSet = (FetchSet<Y>) joins.get(join);
            if (fetchSet == null) {
                throw new IllegalArgumentException(format("join not found: %s", join));
            }
            return fetchSet;
        }

        @Override
        public Map<EntityJoin<X, ?>, FetchSet<?>> getJoins() {
            return unmodifiableMap(joins);
        }

        @Override
        public FetchSet<X> filter(EntityFilter<X> filter) {
            requireNonNull(filter, "filter");
            if (!filter.isAlwaysTrue()) {
                filters.add(filter);
            }
            return this;
        }

        @Override
        public Set<EntityFilter<X>> getFilters() {
            return unmodifiableSet(filters);
        }

        @Override
        public FetchSet<X> orderBy(QueryField<X, ?> field, SortDirection direction) {
            requireNonNull(field, "field");
            requireNonNull(direction, "direction");
            fetchRoot().orderBy.add(new EntityOrderBy<>(this, direction, field::getOrderBy));
            return this;
        }

        @Override
        public FetchSet<X> orderBy(EntitySortKey<X> field, SortDirection direction) {
            requireNonNull(field, "field");
            requireNonNull(direction, "direction");
            fetchRoot().orderBy.add(new EntityOrderBy<>(this, direction, (ctx, builder) -> field.getPath(ctx)));
            return this;
        }

        @Override
        public FetchSet<X> apply(Consumer<FetchSet<X>> consumer) {
            if (consumer != null) {
                consumer.accept(this);
            }
            return this;
        }

        protected abstract RootFetchSetImpl<?> fetchRoot();
    }

    private static final class RootFetchSetImpl<X> extends AbstractFetchSetImpl<X> implements RootFetchSet<X>  {

        private int firstResult;

        private int maxResults = Integer.MAX_VALUE;

        private final List<EntityOrderBy<?>> orderBy = new ArrayList<>();

        @Override
        public RootFetchSet<?> getFetchRoot() {
            return this;
        }

        @Override
        public FetchSet<?> getFetchParent() {
            return null;
        }

        @Override
        public EntityJoin<?, X> getJoinedBy() {
            return null;
        }

        @Override
        public RootFetchSet<X> select(EntityField<X, ?> selection) {
            return (RootFetchSet<X>) super.select(selection);
        }

        @Override
        public <Y> RootFetchSet<X> join(EntityJoin<X, Y> join, Consumer<FetchSet<Y>> consumer) {
            return (RootFetchSet<X>) super.join(join, consumer);
        }

        @Override
        public RootFetchSet<X> filter(EntityFilter<X> filter) {
            return (RootFetchSet<X>) super.filter(filter);
        }

        @Override
        public RootFetchSet<X> orderBy(QueryField<X, ?> field, SortDirection direction) {
            return (RootFetchSet<X>) super.orderBy(field, direction);
        }

        @Override
        public RootFetchSet<X> orderBy(EntitySortKey<X> field, SortDirection direction) {
            return (RootFetchSet<X>) super.orderBy(field, direction);
        }

        @Override
        public int getFirstResult() {
            return firstResult;
        }

        @Override
        public RootFetchSet<X> setFirstResult(int firstResult) {
            this.firstResult = firstResult;
            return this;
        }

        @Override
        public int getMaxResults() {
            return maxResults;
        }

        @Override
        public RootFetchSet<X> setMaxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        @Override
        public List<EntityOrderBy<?>> getOrderBy() {
            return unmodifiableList(orderBy);
        }

        @Override
        public QueryResultList execute(EntityManager entityManager, EntityRoot<X> entityRoot) {
            return new ExecutionContext(entityManager).execute(entityRoot, this);
        }

        @Override
        public RootFetchSet<X> apply(Consumer<FetchSet<X>> consumer) {
            if (consumer != null) {
                consumer.accept(this);
            }
            return this;
        }

        @Override
        protected RootFetchSetImpl<?> fetchRoot() {
            return this;
        }
    }

    private static final class FetchSetImpl<X> extends AbstractFetchSetImpl<X> {

        private final RootFetchSetImpl<?> fetchRoot;

        private final FetchSet<?> parent;

        private final EntityJoin<?, X> joinedBy;

        private FetchSetImpl(RootFetchSetImpl<?> fetchRoot, FetchSet<?> parent, EntityJoin<?, X> joinedBy) {
            this.fetchRoot = requireNonNull(fetchRoot, "fetchRoot");
            this.parent = requireNonNull(parent, "parent");
            this.joinedBy = requireNonNull(joinedBy, "joinedBy");
        }

        @Override
        public RootFetchSet<?> getFetchRoot() {
            return fetchRoot;
        }

        @Override
        public FetchSet<?> getFetchParent() {
            return parent;
        }

        @Override
        public EntityJoin<?, X> getJoinedBy() {
            return joinedBy;
        }

        @Override
        protected RootFetchSetImpl<?> fetchRoot() {
            return fetchRoot;
        }
    }
}
