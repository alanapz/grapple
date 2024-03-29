package org.grapple.invoker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
public @interface GrappleParameter {

    String value() default "";

    String description() default "";

    String deprecated() default "";

    boolean required() default false;

    ParameterWrapping wrapping() default ParameterWrapping.NONE;

    boolean ignore() default false;

}
