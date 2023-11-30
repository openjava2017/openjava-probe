package org.openjava.probe.core.api;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ProbeMethod {
    String PROBE_ID = "probeId";

    int probeId() default 0;
}
