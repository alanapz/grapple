package org.grapple.query;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import org.grapple.core.Chainable;
import org.grapple.query.EntityFieldBuilder.ExpressionResolver;

public interface EntityContext<X> extends Chainable<EntityContext<X>> {

    From<?, X> getEntity();

    <T> Path<T> get(SingularAttribute<? super X, T> attribute);

    <T> Expression<T> get(QueryField<X, T> selection);

    <Y> JoinedEntityContext<Y> join(EntityJoin<X, Y> join);

    <Y> JoinedEntityContext<Y> join(SingularAttribute<X, Y> attribute);

    <Y> AttributeJoin<Y> joinShared(SingularAttribute<X, Y> attribute);

    @Deprecated
    <Y> AttributeJoin<Y> joinUnshared(SingularAttribute<X, Y> attribute);

    <Y> AttributeJoin<Y> joinUnshared(SingularAttribute<X, Y> attribute, Function<Join<X, Y>, Predicate> joinBuilder);

    <Y> AttributeJoin<Y> joinUnshared(SetAttribute<X, Y> attribute, Function<Join<X, Y>, Predicate> joinBuilder);

    <T extends Selection<?>> T addSelection(T selection);

    <T> Expression<T> addSelection(ExpressionResolver<X, T> resolver);

    <T> NonQuerySelection<T> addNonQuerySelection(NonQueryField<X, T> nonQueryField);

    CriteriaQuery<?> getQuery();

    <T> T getQueryParameter(QueryParameter<T> parameter);

    interface AttributeJoin<Y> extends Supplier<Join<?, Y>> {

        <T> Path<T> get(SingularAttribute<? super Y, T> attribute);

        <Z> AttributeJoin<Z> join(SingularAttribute<Y, Z> attribute);

        <Z> AttributeJoin<Z> joinUnshared(SingularAttribute<Y, Z> attribute);

        <Z> AttributeJoin<Z> joinUnshared(SetAttribute<Y, Z> attribute, Consumer<Join<Y, Z>> consumer);
    }
}
