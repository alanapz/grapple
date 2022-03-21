package org.grapple2.sample;

import org.grapple2.GrappleEntity;

@GrappleEntity
public interface EnvironmentVariable {

    String name();

    String value();
}
