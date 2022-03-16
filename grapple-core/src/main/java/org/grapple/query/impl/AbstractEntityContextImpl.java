package org.grapple.query.impl;

import static java.util.Objects.requireNonNull;

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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import org.grapple.query.EntityContext;
import org.grapple.query.EntityField;
import org.grapple.query.EntityFieldBuilder.ExpressionResolver;
import org.grapple.query.EntityJoin;
import org.grapple.query.NonQueryField;
import org.grapple.query.NonQuerySelection;
import org.grapple.query.QueryBuilder;
import org.grapple.query.QueryField;
import org.grapple.query.QueryParameter;
import org.grapple.utils.LazyValue;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple.utils.UnexpectedException;
import org.jetbrains.annotations.NotNull;

abstract class AbstractEntityContextImpl<X> implements EntityContext<X> {

    private final RootFetchSetImpl<?> rootFetchSet;

    private final QueryWrapper queryWrapper;

    private final QueryBuilder queryBuilder;

    private final Supplier<? extends From<?, X>> entity;

    private final Map<QueryField<X, ?>, Expression<?>> selections = new NoDuplicatesMap<>();

    private final Map<EntityJoin<X, ?>, JoinedEntityContextImpl<?>> entityJoins = new NoDuplicatesMap<>();

    private final Map<Attribute<X, ?>, JoinedEntityContextImpl<?>> attributeJoins = new NoDuplicatesMap<>();

    private final Map<Attribute<X, ?>, AttributeJoinImpl<?>> sharedJoins = new NoDuplicatesMap<>();

    private final Map<EntityField<X, ?>, NonQuerySelection<?>> nonQuerySelections = new NoDuplicatesMap<>();

    AbstractEntityContextImpl(@NotNull RootFetchSetImpl<?> rootFetchSet, @NotNull QueryWrapper queryWrapper, @NotNull QueryBuilder queryBuilder, @NotNull Supplier<? extends From<?, X>> entity) {
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
    public <T> Expression<T> get(QueryField<X, T> selection) {
        requireNonNull(selection, "selection");
        final @SuppressWarnings("unchecked") Expression<T> existing = (Expression<T>) selections.get(selection);
        if (existing != null) {
            return existing;
        }
        final Expression<T> expression = selection.getExpression(this, queryBuilder);
        if (expression instanceof Predicate) {
            throw new UnexpectedException("Unexpected predicate");
        }
        selections.put(selection, expression);
        return expression;
    }

    @Override
    public <Y> JoinedEntityContextImpl<Y> join(EntityJoin<X, Y> join) {
        requireNonNull(join, "join");
        final @SuppressWarnings("unchecked") JoinedEntityContextImpl<Y> existing = (JoinedEntityContextImpl<Y>) entityJoins.get(join);
        if (existing != null) {
            return existing;
        }
        final JoinedEntityContextImpl<Y> joinedContext = new JoinedEntityContextImpl<>(rootFetchSet, queryWrapper, queryBuilder, join.join(this, queryBuilder, entity));
        entityJoins.put(join, joinedContext);
        return joinedContext;
    }

    @Override
    public <Y> JoinedEntityContextImpl<Y> join(SingularAttribute<X, Y> attribute) {
        requireNonNull(attribute, "attribute");
        final @SuppressWarnings("unchecked") JoinedEntityContextImpl<Y> existing = (JoinedEntityContextImpl<Y>) attributeJoins.get(attribute);
        if (existing != null) {
            return existing;
        }
        final JoinedEntityContextImpl<Y> joinedContext = new JoinedEntityContextImpl<>(rootFetchSet, queryWrapper, queryBuilder, LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT)));
        attributeJoins.put(attribute, joinedContext);
        return joinedContext;
    }

    @Override
    public <Y> AttributeJoinImpl<Y> joinShared(SingularAttribute<X, Y> attribute) {
        requireNonNull(attribute, "attribute");
        final @SuppressWarnings("unchecked") AttributeJoinImpl<Y> existing = (AttributeJoinImpl<Y>) sharedJoins.get(attribute);
        if (existing != null) {
            return existing;
        }
        final AttributeJoinImpl<Y> attributeJoin = new AttributeJoinImpl<>(LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT)));
        sharedJoins.put(attribute, attributeJoin);
        return attributeJoin;
    }

    @Override
    @Deprecated
    public <Y> AttributeJoin<Y> joinUnshared(SingularAttribute<X, Y> attribute) {
        requireNonNull(attribute, "attribute");
        return new AttributeJoinImpl<>(LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT)));
    }

    @Override
    public <Y> AttributeJoin<Y> joinUnshared(SingularAttribute<X, Y> attribute, Function<Join<X, Y>, Predicate> joinBuilder) {
        requireNonNull(attribute, "attribute");
        requireNonNull(joinBuilder, "joinBuilder");
        return new AttributeJoinImpl<>(LazyValue.of(() -> {
            final Join<X, Y> join = entity.get().join(attribute, JoinType.LEFT);
            join.on(joinBuilder.apply(join));
            return join;
        }));
    }

    @Override
    public <Y> AttributeJoin<Y> joinUnshared(SetAttribute<X, Y> attribute, Function<Join<X, Y>, Predicate> joinBuilder) {
        requireNonNull(attribute, "attribute");
        requireNonNull(joinBuilder, "joinBuilder");
        final Join<X, Y> join = entity.get().join(attribute, JoinType.LEFT); // Eager join, as Hibernate doesn't seem to like delayed evaluation
        join.on(joinBuilder.apply(join));
        return new AttributeJoinImpl<>(LazyValue.fixed(join));
    }

    @Override
    public <T extends Selection<?>> T addSelection(T selection) {
        requireNonNull(selection, "selection");
        queryWrapper.select(selection);
        return selection;
    }

    @Override
    public <T> Expression<T> addSelection(ExpressionResolver<X, T> resolver) {
        requireNonNull(resolver, "resolver");
        final Expression<T> expression = resolver.get(this, queryBuilder);
        queryWrapper.select(expression);
        return expression;
    }

    @Override
    public <T> NonQuerySelection<T> addNonQuerySelection(NonQueryField<X, T> nonQueryField) {
        requireNonNull(nonQueryField, "nonQueryField");
        final @SuppressWarnings("unchecked") NonQuerySelection<T> existing = (NonQuerySelection<T>) nonQuerySelections.get(nonQueryField);
        if (existing != null) {
            return existing;
        }
        // ATTENTION: boundResolver must be completely initialised here- do not "optimise" following line
        // Make sure the get() happens outside lambda
        final NonQuerySelection<T> nonQuerySelection = nonQueryField.getResolver().get(AbstractEntityContextImpl.this, queryBuilder)::apply;
        nonQuerySelections.put(nonQueryField, nonQuerySelection);
        return nonQuerySelection;
    }

    @Override
    public void addRestriction(Predicate restriction) {
        requireNonNull(restriction, "restriction");
        queryWrapper.where(restriction);
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
    public <Z> Z invoke(@NotNull Function<EntityContext<X>, Z> function) {
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
            final @SuppressWarnings("unchecked") AttributeJoin<Z> existing = (AttributeJoin<Z>) attributeJoins.get(attribute);
            if (existing != null) {
                return existing;
            }
            final AttributeJoin<Z> attributeJoin = new AttributeJoinImpl<>(LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT)));
            attributeJoins.put(attribute, attributeJoin);
            return attributeJoin;
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
