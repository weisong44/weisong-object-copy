package com.weisong.common.vodo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Makes a VO field of type List<Long> to be used for content optimization
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ContentOptimization {

    /**
     * Name of the DO field
     */
    String value();
    
}
