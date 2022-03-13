/*
package org.grapple.schema.impl;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import graphql.schema.DataFetchingEnvironment;
import org.grapple.query.RootFetchSet;
import org.grapple.schema.EntityQueryExecutionListener;
import org.grapple.schema.EntityQueryExecutionListener.QueryListenerContext;

final class EntityQueryExecutionListeners {

    private final List<EntityQueryExecutionListener> listeners = new ArrayList<>();

    EntityQueryExecutionListeners copy() {
        final EntityQueryExecutionListeners copy = new EntityQueryExecutionListeners();
        copy.listeners.addAll(listeners);
        return copy;
    }

    void addListener(EntityQueryExecutionListener listener) {
        requireNonNull(listener, "listener");
        listeners.add(listener);
    }

    QueryListenerContext queryStarted(DataFetchingEnvironment environment, RootFetchSet<?> fetchSet, String queryName, Object queryTag) {
        requireNonNull(fetchSet, "fetchSet");
        requireNonNull(queryName, "queryName");
        final List<QueryListenerContext> callbacks = new ArrayList<>();
        for (EntityQueryExecutionListener listener: listeners) {
            final QueryListenerContext callback = listener.queryStarted(environment, fetchSet, queryName, queryTag);
            if (callback != null) {
                callbacks.add(callback);
            }
        }
        return new QueryListenerContextImpl(callbacks);
    }

    private static class QueryListenerContextImpl implements QueryListenerContext {

        private final List<QueryListenerContext> callbacks;

        private QueryListenerContextImpl(List<QueryListenerContext> callbacks) {
            this.callbacks = new ArrayList<>(callbacks);
        }

        @Override
        public void complete(Map<String, Object> response) {
            callbacks.forEach(callback -> callback.complete(response));
        }

        @Override
        public void error(Exception e) {
            callbacks.forEach(callback -> callback.error(e));
        }
    }
}
*/