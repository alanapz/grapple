package org.grapple.query;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.grapple.core.Chainable;
import org.jetbrains.annotations.NotNull;

public interface QueryResultRow<X> extends Chainable<QueryResultRow<X>> {

    boolean isExists();

    <T> T get(@NotNull EntityField<X, T> field);

    <T> T getIfNotNull(@NotNull EntityField<X, T> field, T valueIfNull);

    <Y> QueryResultRow<Y> getJoin(@NotNull EntityJoin<X, Y> join);

    <Y> void applyJoinIfExists(@NotNull EntityJoin<X, Y> join, @NotNull Consumer<QueryResultRow<Y>> consumer);

    <Y, T> T applyJoinIfExists(@NotNull EntityJoin<X, Y> join, T value, @NotNull BiConsumer<QueryResultRow<Y>, T> consumer);

}
