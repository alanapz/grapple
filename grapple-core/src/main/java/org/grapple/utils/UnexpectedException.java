package org.grapple.utils;

public class UnexpectedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnexpectedException(String message) {
        super(message);
    }
}
