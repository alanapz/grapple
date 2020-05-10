package org.grapple.invoker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface GrappleMutation {

    String value() default "";

    String description() default "";

    String deprecated() default "";

    boolean ignore() default false;

}
