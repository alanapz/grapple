package org.grapple.query.impl;

import static java.util.Objects.requireNonNull;
import static org.jooq.lambda.Seq.seq;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.grapple.query.QueryResultList;
import org.grapple.query.QueryResultRow;
import org.grapple.query.RootFetchSet;
import org.grapple.query.TabularResultList;
import org.grapple.schema.NonScalarQueryResultException;
import org.jetbrains.annotations.NotNull;

final class QueryResultListImpl<X> implements QueryResultList<X> {

    private final RootFetchSet<X> rootFetchSet;

    private final int totalResults;

    private final List<TabularResultRowImpl> results;

    QueryResultListImpl(RootFetchSet<X> rootFetchSet, int totalResults, List<TabularResultRowImpl> results) {
        requireNonNull(rootFetchSet, "rootFetchSet");
        requireNonNull(results, "results");
        this.rootFetchSet = rootFetchSet;
        this.totalResults = totalResults;
        this.results = results;
    }

    @Override
    public int getTotalResults() {
        return totalResults;
    }

    @Override
    public int getRowsRetrieved() {
        return results.size();
    }

    @Override
    public <U> List<U> map(Function<QueryResultRow<X>, U> resultMapper) {
        requireNonNull(resultMapper, "resultMapper");
        return seq(results).map(result -> new QueryResultRowImpl<>(rootFetchSet, result)).map(resultMapper).toList();
    }

    @Override
    public <U> List<U> map(Supplier<U> objSupplier, BiConsumer<QueryResultRow<X>, U> consumer) {
        requireNonNull(objSupplier, "objSupplier");
        requireNonNull(consumer, "consumer");
        return seq(results).map(result -> {
            final U instance = objSupplier.get();
            consumer.accept(new QueryResultRowImpl<>(rootFetchSet, result), instance);
            return instance;
        }).toList();
    }

    @Override
    public Optional<QueryResultRow<X>> getUniqueResult() {
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() != 1) {
            throw new NonScalarQueryResultException("Query returned multiple rows");
        }
        return Optional.of(new QueryResultRowImpl<>(rootFetchSet, results.get(0)));
    }

    @Override
    public TabularResultList asTabular() {
        return new TabularResultListImpl(totalResults, results);
    }

    @NotNull
    @Override
    public Iterator<QueryResultRow<X>> iterator() {
        return seq(results).<QueryResultRow<X>> map(result -> new QueryResultRowImpl<>(rootFetchSet, result)).iterator();
    }
}
