package org.grapple.invoker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GrappleFilter {

    String value() default "";

    String description() default "";

    String deprecated() default "";

    boolean required() default false;

    boolean ignore() default false;

}
