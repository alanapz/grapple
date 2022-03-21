package org.grapple2.sample;

import java.util.List;
import java.util.Optional;

import org.grapple2.GrappleEntity;

@GrappleEntity
public interface EnvironmentVariableQuery {

    List<EnvironmentVariable> getEnvironmentVariables();

    Optional<EnvironmentVariable> getEnvironmentVariable(String name);
}
