package org.grapple.query;

import org.grapple.core.GrappleException;

public final class NonScalarQueryResultException extends GrappleException {

    private static final long serialVersionUID = 1L;

    public NonScalarQueryResultException(String message) {
        super(message);
    }
}
