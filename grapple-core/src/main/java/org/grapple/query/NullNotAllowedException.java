package org.grapple.query;

import org.grapple.core.GrappleException;

public final class NullNotAllowedException extends GrappleException {

    private static final long serialVersionUID = 1L;

    public NullNotAllowedException(String message) {
        super(message);
    }
}
