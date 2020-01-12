package org.grapple.core;

public abstract class GrappleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GrappleException(String message) {
        super(message);
    }
}
