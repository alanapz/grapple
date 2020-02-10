package org.grapple.core;

import java.util.Set;

@FunctionalInterface
public interface ElementVisibility {

    boolean isVisible(Set<String> rolesHeld);

}
