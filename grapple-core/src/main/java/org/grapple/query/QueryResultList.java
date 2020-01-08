package org.grapple.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public final class QueryResultList implements Iterable<QueryResultItem> {

    private final int totalResults;

    private final List<QueryResultItem> results;

    private QueryResultList(int totalResults, List<QueryResultItem> results) {
        this.totalResults = totalResults;
        this.results = results;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public List<QueryResultItem> getResults() {
        return results;
    }

    @NotNull
    @Override
    public Iterator<QueryResultItem> iterator() {
        return results.iterator();
    }

    public <U> List<U> mapResults(Function<QueryResultItem, U> resultMapper) {
        requireNonNull(resultMapper, "resultMapper");
        return results.stream().map(resultMapper).collect(Collectors.toList());
    }

    public <U> List<U> mapResults(Supplier<U> objSupplier, BiConsumer<QueryResultItem, U> consumer) {
        requireNonNull(objSupplier, "objSupplier");
        requireNonNull(consumer, "consumer");
        return results.stream().map(result -> {
            final U instance = objSupplier.get();
            consumer.accept(result, instance);
            return instance;
        }).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return format("total=%d,results=%s", totalResults, results);
    }

    /* package */ static QueryResultList empty(int totalResults) {
        return new QueryResultList(totalResults, emptyList());
    }

    /* package */ static QueryResultList results(int totalResults) {
        return new QueryResultList(totalResults, new ArrayList<>());
    }
}
