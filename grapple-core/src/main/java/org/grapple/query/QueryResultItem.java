package org.grapple.query;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jooq.lambda.tuple.Tuple2;

import static java.util.Objects.requireNonNull;
import static org.grapple.query.Utils.cast;
import static org.grapple.utils.Utils.coalesce;

public final class QueryResultItem {

    private final Map<Tuple2<FetchSet<?>, EntityField<?, ?>>, Object> values = new HashMap<>();

    private final Map<String, Object> valuesByName = new LinkedHashMap<>();

    @SuppressWarnings("SuspiciousMethodCalls")
    public <X, T> T get(FetchSet<X> fetchSet, EntityField<X, T> field) {
        requireNonNull(fetchSet, "fetchSet");
        requireNonNull(field, "field");
        final Tuple2<FetchSet<X>, EntityField<X, T>> key = new Tuple2<>(fetchSet, field);
        if (!values.containsKey(key)) {
            throw new IllegalArgumentException(String.format("Requested element not fetched: %s", Utils.getFullName(fetchSet, field.getName())));
        }
        return cast(values.get(key));
    }

    public <X, T> T getIfNotNull(FetchSet<X> fetchSet, EntityField<X, T> field, T valueIfNull) {
        return coalesce(get(fetchSet, field), valueIfNull);
    }

    <X, T> void set(FetchSet<X> fetchSet, EntityField<X, T> field, T value) {
        values.put(new Tuple2<>(fetchSet, field), value);
        valuesByName.put(Utils.getFullName(fetchSet, field.getName()), value);
    }

    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(valuesByName);
    }

    @Override
    public String toString() {
        return String.valueOf(valuesByName);
    }
}
