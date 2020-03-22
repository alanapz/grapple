package org.grapple.reflect;

import org.grapple.core.GrappleException;

public final class UnmappableTypeException extends GrappleException {

    private static final long serialVersionUID = 1L;

    public UnmappableTypeException(String message) {
        super(message);
    }
}
