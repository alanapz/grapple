package org.grapple.schema;

import static java.lang.String.format;

import java.lang.reflect.Method;
import org.grapple.core.GrappleException;

public final class QueryExecutionException extends GrappleException {

    private static final long serialVersionUID = 1L;

    public QueryExecutionException(Method method, Throwable e) {
        super(format("Couldn't invoke: %s", method), e);
    }
}
