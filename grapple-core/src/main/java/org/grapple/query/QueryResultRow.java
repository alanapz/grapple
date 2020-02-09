package org.grapple.query;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.grapple.core.Chainable;

public interface QueryResultRow<X> extends Chainable<QueryResultRow<X>> {

    boolean isExists();

    <T> T get(EntityField<X, T> field);

    <T> T getIfNotNull(EntityField<X, T> field, T valueIfNull);

    <Y> QueryResultRow<Y> getJoin(EntityJoin<X, Y> join);

    <Y> void applyJoinIfExists(EntityJoin<X, Y> join, Consumer<QueryResultRow<Y>> consumer);

    <Y, T> T applyJoinIfExists(EntityJoin<X, Y> join, T value, BiConsumer<QueryResultRow<Y>, T> consumer);

}
