package org.grapple.core;

import org.grapple.authz.Principal;

@FunctionalInterface
public interface ElementVisibility {

    boolean isVisible(Principal principal);

}
