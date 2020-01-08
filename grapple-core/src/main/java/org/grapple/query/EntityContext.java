package org.grapple.query;

import java.util.function.Supplier;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import org.grapple.utils.Chainable;

public interface EntityContext<X> extends Chainable<EntityContext<X>> {

    From<?, X> getEntity();

    <T> Path<T> get(SingularAttribute<? super X, T> attribute);

    <T> Expression<T> get(QueryField<X, T> selection);

    <Y> EntityContext<Y> join(EntityJoin<X, Y> join);

    <Y> AttributeJoin<Y> join(SingularAttribute<X, Y> attribute);

    <Y> AttributeJoin<Y> joinUnshared(SingularAttribute<X, Y> attribute);

    <Y> AttributeJoin<Y> joinUnshared(SetAttribute<X, Y> attribute);

    <T extends Selection<?>> T addSelection(T selection);

    CriteriaQuery<?> getQuery();

    interface AttributeJoin<Y> extends Supplier<Join<?, Y>> {

        <T> Path<T> get(SingularAttribute<? super Y, T> attribute);

        <Z> AttributeJoin<Z> join(SingularAttribute<Y, Z> attribute);

        <Z> AttributeJoin<Z> joinUnshared(SingularAttribute<Y, Z> attribute);

        <Z> AttributeJoin<Z> joinUnshared(SetAttribute<Y, Z> attribute);
    }
}
