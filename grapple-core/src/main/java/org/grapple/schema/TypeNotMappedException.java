package org.grapple.schema;

import org.grapple.core.GrappleException;

public final class TypeNotMappedException extends GrappleException {

    private static final long serialVersionUID = 1L;

    public TypeNotMappedException(String message) {
        super(message);
    }

    public TypeNotMappedException(String message, Throwable cause) {
        super(message, cause);
    }

}
