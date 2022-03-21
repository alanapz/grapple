package org.grapple.query;

import org.jetbrains.annotations.NotNull;

public interface QueryProvider {

    <X> RootFetchSet<X> newQuery(@NotNull Class<X> entityClass);
}
