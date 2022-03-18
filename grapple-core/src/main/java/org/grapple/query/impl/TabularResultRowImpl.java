package org.grapple.query.impl;

import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.coalesce;
import static org.grapple.utils.Utils.readOnlyCopy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.grapple.query.EntityField;
import org.grapple.query.FetchSet;
import org.grapple.query.TabularResultRow;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple.utils.NoDuplicatesSet;
import org.jetbrains.annotations.NotNull;
import org.jooq.lambda.tuple.Tuple2;

final class TabularResultRowImpl implements TabularResultRow {

    private final Set<FetchSet<?>> entitiesExists = new NoDuplicatesSet<>();

    private final Map<Tuple2<FetchSet<?>, EntityField<?, ?>>, Object> values = new NoDuplicatesMap<>();

    private final Map<String, Object> valuesByName = new LinkedHashMap<>();

    @Override
    public <X> boolean isExists(FetchSet<X> fetchSet) {
        requireNonNull(fetchSet, "fetchSet");
        return entitiesExists.contains(fetchSet);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X, T> T get(FetchSet<X> fetchSet, EntityField<X, T> field) {
        requireNonNull(fetchSet, "fetchSet");
        requireNonNull(field, "field");
        final Tuple2<FetchSet<?>, EntityField<?, ?>> key = new Tuple2<>(fetchSet, field);
        if (!values.containsKey(key)) {
            throw new IllegalArgumentException(String.format("Requested element not fetched: %s", QueryImplUtils.resolveFullName(fetchSet, field.getName())));
        }
        return (T) values.get(key);
    }

    @Override
    public <X, T> T getIfNotNull(@NotNull FetchSet<X> fetchSet, @NotNull EntityField<X, T> field, T valueIfNull) {
        return coalesce(get(fetchSet, field), valueIfNull);
    }

    @Override
    public Map<String, Object> getValues() {
        return readOnlyCopy(valuesByName);
    }

    <X, T> void setValue(@NotNull FetchSet<X> fetchSet, @NotNull EntityField<X, T> field, T value) {
        values.put(new Tuple2<>(fetchSet, field), value);
        valuesByName.put(QueryImplUtils.resolveFullName(fetchSet, field.getName()), value);
    }

    <X> void setEntityExists(@NotNull FetchSet<X> fetchSet) {
        entitiesExists.add(fetchSet);
    }

    @Override
    public String toString() {
        return valuesByName.toString();
    }
}
