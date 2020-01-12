package org.grapple.query.impl;

import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.uncheckedCast;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import org.grapple.query.EntityContext;
import org.grapple.query.EntityJoin;
import org.grapple.query.JoinedEntityContext;
import org.grapple.query.QueryBuilder;
import org.grapple.query.QueryField;
import org.grapple.query.QueryParameter;
import org.grapple.utils.LazyValue;
import org.grapple.utils.NoDuplicatesMap;

abstract class AbstractEntityContextImpl<X> implements EntityContext<X> {

    private final RootFetchSetImpl<?> rootFetchSet;

    private final QueryWrapper queryWrapper;

    private final QueryBuilder queryBuilder;

    private final Supplier<? extends From<?, X>> entity;

    private final Map<QueryField<?, ?>, Expression<?>> selections = new NoDuplicatesMap<>();

    private final Map<EntityJoin<?, ?>, JoinedEntityContextImpl<?>> entityJoins = new NoDuplicatesMap<>();

    private final Map<Attribute<?, ?>, JoinedEntityContextImpl<?>> attributeJoins = new NoDuplicatesMap<>();

    private final Map<Attribute<?, ?>, AttributeJoin<?>> sharedJoins = new NoDuplicatesMap<>();

    AbstractEntityContextImpl(RootFetchSetImpl<?> rootFetchSet, QueryWrapper queryWrapper, QueryBuilder queryBuilder, Supplier<? extends From<?, X>> entity) {
        this.rootFetchSet = requireNonNull(rootFetchSet, "rootFetchSet");
        this.queryWrapper = requireNonNull(queryWrapper, "queryWrapper");
        this.queryBuilder = requireNonNull(queryBuilder, "queryBuilder");
        this.entity = requireNonNull(entity, "entity");
    }

    @Override
    public From<?, X> getEntity() {
        return entity.get();
    }

    @Override
    public <T> Path<T> get(SingularAttribute<? super X, T> attribute) {
        requireNonNull(attribute, "attribute");
        return entity.get().get(attribute);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Expression<T> get(QueryField<X, T> selection) {
        requireNonNull(selection, "selection");
        final Expression<T> existing = (Expression<T>) selections.get(selection);
        if (existing != null) {
            return existing;
        }
        final Expression<T> expression = selection.getExpression(this, queryBuilder);
        selections.put(selection, expression);
        return expression;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> JoinedEntityContext<Y> join(EntityJoin<X, Y> join) {
        requireNonNull(join, "join");
        if (entityJoins.containsKey(join)) {
            return (JoinedEntityContextImpl<Y>) entityJoins.get(join);
        }
        final JoinedEntityContextImpl<Y> joinedContext = new JoinedEntityContextImpl<>(rootFetchSet, queryWrapper, queryBuilder, join.join(this, queryBuilder, entity));
        entityJoins.put(join, joinedContext);
        return joinedContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> JoinedEntityContext<Y> join(SingularAttribute<X, Y> attribute) {
        requireNonNull(attribute, "attribute");
        if (attributeJoins.containsKey(attribute)) {
            return (JoinedEntityContextImpl<Y>) attributeJoins.get(attribute);
        }
        final JoinedEntityContextImpl<Y> joinedContext = new JoinedEntityContextImpl<>(rootFetchSet, queryWrapper, queryBuilder, LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT)));
        attributeJoins.put(attribute, joinedContext);
        return joinedContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> AttributeJoin<Y> joinShared(SingularAttribute<X, Y> attribute) {
        requireNonNull(attribute, "attribute");
        if (sharedJoins.containsKey(attribute)) {
            return (AttributeJoin<Y>) sharedJoins.get(attribute);
        }
        final AttributeJoin<Y> attributeJoin = new AttributeJoinImpl<>(LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT)));
        sharedJoins.put(attribute, attributeJoin);
        return attributeJoin;
    }

    @Override
    public <Y> AttributeJoin<Y> joinUnshared(SingularAttribute<X, Y> attribute) {
        requireNonNull(attribute, "attribute");
        return new AttributeJoinImpl<>(LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT)));
    }

    @Override
    public <Y> AttributeJoin<Y> joinUnshared(SetAttribute<X, Y> attribute, Consumer<Join<X, Y>> consumer) {
        requireNonNull(attribute, "attribute");
        return new AttributeJoinImpl<>(LazyValue.of(() -> {
            final Join<X, Y> join = entity.get().join(attribute, JoinType.LEFT);
            if (consumer != null) {
                consumer.accept(join);
            }
            return join;
        }));
    }

    @Override
    public <T extends Selection<?>> T addSelection(T selection) {
        requireNonNull(selection, "selection");
        queryWrapper.select(selection);
        return selection;
    }

    @Override
    public CriteriaQuery<?> getQuery() {
        return queryWrapper.getQuery();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getQueryParameter(QueryParameter<T> parameter) {
        requireNonNull(parameter, "parameter");
        return (T) rootFetchSet.getQueryParameters().get(parameter);
    }

    @Override
    public EntityContext<X> apply(Consumer<EntityContext<X>> consumer) {
        return org.grapple.utils.Utils.apply(this, consumer);
    }

    @Override
    public <Z> Z invoke(Function<EntityContext<X>, Z> function) {
        return requireNonNull(function, "function").apply(this);
    }

    private static final class AttributeJoinImpl<Y> implements AttributeJoin<Y> {

        private final LazyValue<Join<?, Y>> entity;

        private final Map<Attribute<?, ?>, AttributeJoin<?>> attributeJoins = new HashMap<>();

        private AttributeJoinImpl(LazyValue<Join<?, Y>> entity) {
            this.entity = requireNonNull(entity, "entity");
        }

        @Override
        public Join<?, Y> get() {
            return entity.get();
        }

        @Override
        public <T> Path<T> get(SingularAttribute<? super Y, T> attribute) {
            requireNonNull(attribute, "attribute");
            return entity.get().get(attribute);
        }

        @Override
        public <Z> AttributeJoin<Z> join(SingularAttribute<Y, Z> attribute) {
            requireNonNull(attribute, "attribute");
            return uncheckedCast(attributeJoins.computeIfAbsent(attribute, unused -> new AttributeJoinImpl<>(LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT)))));
        }

        @Override
        public <Z> AttributeJoin<Z> joinUnshared(SingularAttribute<Y, Z> attribute) {
            requireNonNull(attribute, "attribute");
            return new AttributeJoinImpl<>(LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT)));
        }

        @Override
        public <Z> AttributeJoin<Z> joinUnshared(SetAttribute<Y, Z> attribute, Consumer<Join<Y, Z>> consumer) {
            requireNonNull(attribute, "attribute");
            return new AttributeJoinImpl<>(LazyValue.of(() -> {
                final Join<Y, Z> join = entity.get().join(attribute, JoinType.LEFT);
                if (consumer != null) {
                    consumer.accept(join);
                }
                return join;
            }));
        }
    }
}
