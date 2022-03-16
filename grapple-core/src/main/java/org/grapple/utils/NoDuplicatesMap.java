package org.grapple.utils;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public final class NoDuplicatesMap<K, V> implements Map<K, V> {

    private final Map<K, V> backingMap;

    private final Function<? super K, RuntimeException> onDuplicate;

    private static final Function<Object, RuntimeException> defaultOnDuplicate = key -> new IllegalArgumentException(format("Duplicate key: %s", key));

    public NoDuplicatesMap() {
        this(new HashMap<>(), defaultOnDuplicate);
    }

    public NoDuplicatesMap(Map<K, V> backingMap) {
        this(backingMap, defaultOnDuplicate);
    }

    public NoDuplicatesMap(Function<? super K, RuntimeException> onDuplicate) {
        this(new HashMap<>(), onDuplicate);
    }

    public NoDuplicatesMap(Map<K, V> backingMap, Function<? super K, RuntimeException> onDuplicate) {
        this.backingMap = requireNonNull(backingMap);
        this.onDuplicate = requireNonNull(onDuplicate);
    }

    private void checkNotExists(K key) {
        if (backingMap.containsKey(key)) {
            throw onDuplicate.apply(key);
        }
    }

    @Override
    public int size() {
        return backingMap.size();
    }

    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return backingMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return backingMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return backingMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        checkNotExists(key);
        return backingMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return backingMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.keySet().forEach(this::checkNotExists);
        backingMap.putAll(m);
    }

    @Override
    public void clear() {
        backingMap.clear();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return backingMap.keySet();
    }

    @Override
    public @NotNull Collection<V> values() {
        return backingMap.values();
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        return backingMap.entrySet();
    }
}
