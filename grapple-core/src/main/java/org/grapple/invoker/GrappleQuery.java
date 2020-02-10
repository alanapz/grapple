package org.grapple.invoker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.grapple.schema.EntityQueryType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface GrappleQuery {

    String value() default "";

    EntityQueryType type() default EntityQueryType.DEFAULT;

    String description() default "";

    String deprecated() default "";

    boolean ignore() default false;

}
