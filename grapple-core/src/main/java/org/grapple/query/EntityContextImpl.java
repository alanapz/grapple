package org.grapple.query;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
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
import org.grapple.utils.LazyValue;

import static java.util.Objects.requireNonNull;
import static org.grapple.query.Utils.cast;

final class EntityContextImpl<X> implements EntityContext<X> {

    private final QueryWrapper query;

    private final QueryBuilder builder;

    private final Supplier<? extends From<?, X>> entity;

    private final Map<QueryField<?, ?>, Expression<?>> selections = new HashMap<>();

    private final Map<EntityJoin<?, ?>, EntityContext<?>> entityJoins = new HashMap<>();

    private final Map<Attribute<?, ?>, AttributeJoin<?>> attributeJoins = new HashMap<>();

    EntityContextImpl(QueryWrapper query, QueryBuilder builder, Supplier<? extends From<?, X>> entity) {
        this.query = requireNonNull(query, "query");
        this.builder = requireNonNull(builder, "builder");
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
        return cast(selections.computeIfAbsent(selection, unused -> selection.getExpression(this, builder)));
    }

    @Override
    public <Y> EntityContext<Y> join(EntityJoin<X, Y> join) {
        requireNonNull(join, "join");
        return cast(entityJoins.computeIfAbsent(join, unused -> new EntityContextImpl<>(query, builder, join.join(this, builder, entity))));
    }

    @Override
    public <Y> AttributeJoin<Y> join(SingularAttribute<X, Y> attribute) {
        requireNonNull(attribute, "attribute");
        return cast(attributeJoins.computeIfAbsent(attribute, unused -> new AttributeJoinImpl<>(LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT)))));
    }

    @Override
    public <Y> AttributeJoin<Y> joinUnshared(SingularAttribute<X, Y> attribute) {
        requireNonNull(attribute, "attribute");
        return new AttributeJoinImpl<>(LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT)));
    }

    @Override
    public <Y> AttributeJoin<Y> joinUnshared(SetAttribute<X, Y> attribute) {
        requireNonNull(attribute, "attribute");
        return new AttributeJoinImpl<>(LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT)));
    }

    @Override
    public <T extends Selection<?>> T addSelection(T selection) {
        requireNonNull(selection, "selection");
        query.select(selection);
        return selection;
    }

    @Override
    public CriteriaQuery<?> getQuery() {
        return query.getQuery();
    }

    @Override
    public EntityContext<X> apply(Consumer<EntityContext<X>> consumer) {
        if (consumer != null) {
            consumer.accept(this);
        }
        return this;
    }

    private static final class AttributeJoinImpl<Y> implements AttributeJoin<Y> {

        private final LazyValue<Join<?, Y>> join;

        private final Map<Attribute<?, ?>, AttributeJoin<?>> attributeJoins = new HashMap<>();

        private AttributeJoinImpl(LazyValue<Join<?, Y>> join) {
            this.join = requireNonNull(join, "join");
        }

        @Override
        public Join<?, Y> get() {
            return join.get();
        }

        @Override
        public <T> Path<T> get(SingularAttribute<? super Y, T> attribute) {
            requireNonNull(attribute, "attribute");
            return join.get().get(attribute);
        }

        @Override
        public <Z> AttributeJoin<Z> join(SingularAttribute<Y, Z> attribute) {
            requireNonNull(attribute, "attribute");
            return cast(attributeJoins.computeIfAbsent(attribute, unused -> new AttributeJoinImpl<>(LazyValue.of(() -> join.get().join(attribute, JoinType.LEFT)))));
        }

        @Override
        public <Z> AttributeJoin<Z> joinUnshared(SingularAttribute<Y, Z> attribute) {
            requireNonNull(attribute, "attribute");
            return new AttributeJoinImpl<>(LazyValue.of(() -> join.get().join(attribute, JoinType.LEFT)));
        }

        @Override
        public <Z> AttributeJoin<Z> joinUnshared(SetAttribute<Y, Z> attribute) {
            requireNonNull(attribute, "attribute");
            return new AttributeJoinImpl<>(LazyValue.of(() -> join.get().join(attribute, JoinType.LEFT)));
        }
    }
}
