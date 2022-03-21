package org.grapple2.core;

@FunctionalInterface
public interface FieldVisibility {

    boolean isVisible(Principal principal);

}
