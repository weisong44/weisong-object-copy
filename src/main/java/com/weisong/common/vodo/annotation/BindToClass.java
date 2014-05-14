package com.weisong.common.vodo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface BindToClass {
    /**
     * Bind name of the primary model class, this binds a VO to a DO. This is
     * only necessary when classes do not use our standard naming convention:
     *
     * VO: com.xxx.(subpackages).vo.(classname)Vo DO:
     * com.xxx.(subpackages).(classname)
     *
     * Whenever possible, the standard naming convention should be used. This
     * should only be used in truly exceptional cases.
     */
    String value();
}
