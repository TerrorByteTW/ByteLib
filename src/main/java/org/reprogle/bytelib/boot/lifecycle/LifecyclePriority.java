package org.reprogle.bytelib.boot.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LifecyclePriority {
    PluginLifecycle.Priority value() default PluginLifecycle.Priority.NORMAL;
}
