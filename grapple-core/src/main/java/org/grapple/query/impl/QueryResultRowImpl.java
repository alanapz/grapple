package org.grapple.query.impl;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;
import org.grapple.query.FetchSet;
import org.grapple.query.QueryResultRow;

import org.jetbrains.annotations.NotNull;

final class QueryResultRowImpl<X> implements QueryResultRow<X> {

    private final FetchSet<X> fetchSet;

    private final TabularResultRowImpl resultRow;

    QueryResultRowImpl(@NotNull FetchSet<X> fetchSet, @NotNull TabularResultRowImpl resultRow) {
        this.fetchSet = fetchSet;
        this.resultRow = resultRow;
    }

    @Override
    public boolean isExists() {
        return resultRow.isExists(fetchSet);
    }

    @Override
    public <T> T get(@NotNull EntityField<X, T> field) {
        return resultRow.get(fetchSet, field);
    }

    @Override
    public <T> T getIfNotNull(@NotNull EntityField<X, T> field, T valueIfNull) {
        return resultRow.getIfNotNull(fetchSet, field, valueIfNull);
    }

    @Override
    public <Y> QueryResultRow<Y> getJoin(@NotNull EntityJoin<X, Y> join) {
        final FetchSet<Y> joinedFetchSet = fetchSet.getJoin(join);
        if (joinedFetchSet == null) {
            throw new IllegalArgumentException(String.format("Join not fetched: %s", QueryImplUtils.resolveFullName(fetchSet, join.getName())));
        }
        return new QueryResultRowImpl<>(joinedFetchSet, resultRow);
    }

    @Override
    public <Y> void applyJoinIfExists(@NotNull EntityJoin<X, Y> join, @NotNull Consumer<QueryResultRow<Y>> consumer) {
        final QueryResultRow<Y> joinedRow = getJoin(join);
        if (joinedRow.isExists()) {
            consumer.accept(joinedRow);
        }
    }

    @Override
    public <Y, T> T applyJoinIfExists(@NotNull EntityJoin<X, Y> join, T value, @NotNull BiConsumer<QueryResultRow<Y>, T> consumer) {
        final QueryResultRow<Y> joinedRow = getJoin(join);
        if (joinedRow.isExists()) {
            consumer.accept(joinedRow, value);
        }
        return value;
    }

    @Override
    public String toString() {
        return resultRow.toString();
    }
}
