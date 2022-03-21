package org.grapple.query.impl;

import static java.util.Objects.requireNonNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;
import org.grapple.query.FetchSet;
import org.grapple.query.QueryResultRow;

final class QueryResultRowImpl<X> implements QueryResultRow<X> {

    private final FetchSet<X> fetchSet;

    private final TabularResultRowImpl resultRow;

    QueryResultRowImpl(FetchSet<X> fetchSet, TabularResultRowImpl resultRow) {
        this.fetchSet = requireNonNull(fetchSet, "fetchSet");
        this.resultRow = requireNonNull(resultRow, "resultRow");
    }

    @Override
    public boolean isExists() {
        return resultRow.isExists(fetchSet);
    }

    @Override
    public <T> T get(EntityField<X, T> field) {
        requireNonNull(field, "field");
        return resultRow.get(fetchSet, field);
    }

    @Override
    public <T> T getIfNotNull(EntityField<X, T> field, T valueIfNull) {
        requireNonNull(field, "field");
        return resultRow.getIfNotNull(fetchSet, field, valueIfNull);
    }

    @Override
    public <Y> QueryResultRow<Y> getJoin(EntityJoin<X, Y> join) {
        requireNonNull(join, "join");
        final FetchSet<Y> joinedFetchSet = fetchSet.getJoin(join);
        if (joinedFetchSet == null) {
            throw new IllegalArgumentException(String.format("Join not fetched: %s", QueryImplUtils.resolveFullName(fetchSet, join.getName())));
        }
        return new QueryResultRowImpl<>(joinedFetchSet, resultRow);
    }

    @Override
    public <Y> void applyJoinIfExists(EntityJoin<X, Y> join, Consumer<QueryResultRow<Y>> consumer) {
        requireNonNull(join, "join");
        requireNonNull(consumer, "consumer");
        final QueryResultRow<Y> joinedRow = getJoin(join);
        if (joinedRow.isExists()) {
            consumer.accept(joinedRow);
        }
    }

    @Override
    public <Y, T> T applyJoinIfExists(EntityJoin<X, Y> join, T value, BiConsumer<QueryResultRow<Y>, T> consumer) {
        requireNonNull(join, "join");
        requireNonNull(consumer, "consumer");
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
