package org.grapple.utils;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

public class NoDuplicatesSet<T> implements Set<T> {

    private final Set<T> backingSet;

    private final Function<? super T, RuntimeException> onDuplicate;

    private static final Function<Object, RuntimeException> defaultOnDuplicate = key -> new IllegalArgumentException(format("Duplicate key: %s", key));

    public NoDuplicatesSet() {
        this(new HashSet<>(), defaultOnDuplicate);
    }

    public NoDuplicatesSet(Function<? super T, RuntimeException> onDuplicate) {
        this(new HashSet<>(), onDuplicate);
    }

    public NoDuplicatesSet(Set<T> backingSet, Function<? super T, RuntimeException> onDuplicate) {
        this.backingSet = requireNonNull(backingSet);
        this.onDuplicate = requireNonNull(onDuplicate);
    }

    @Override
    public int size() {
        return backingSet.size();
    }

    @Override
    public boolean isEmpty() {
        return backingSet.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return backingSet.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return backingSet.iterator();
    }

    @Override
    public Object[] toArray() {
        return backingSet.toArray();
    }

    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return backingSet.toArray(a);
    }

    @Override
    public boolean add(T t) {
        checkNotExists(t);
        return backingSet.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return backingSet.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return backingSet.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        c.forEach(this::checkNotExists);
        return backingSet.addAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return backingSet.retainAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return backingSet.removeAll(c);
    }

    @Override
    public void clear() {
        backingSet.clear();
    }

    private void checkNotExists(T t) {
        if (backingSet.contains(t)) {
            throw onDuplicate.apply(t);
        }
    }
}
